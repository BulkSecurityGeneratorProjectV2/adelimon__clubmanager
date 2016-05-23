package com.ajdconsulting.pra.clubmanager.web.rest;

import com.ajdconsulting.pra.clubmanager.domain.EarnedPoints;
import com.ajdconsulting.pra.clubmanager.domain.Member;
import com.ajdconsulting.pra.clubmanager.repository.EarnedPointsRepository;
import com.ajdconsulting.pra.clubmanager.repository.MemberRepository;
import com.ajdconsulting.pra.clubmanager.security.SecurityUtils;
import com.ajdconsulting.pra.clubmanager.web.rest.util.HeaderUtil;
import com.ajdconsulting.pra.clubmanager.web.rest.util.PaginationUtil;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing EarnedPoints.
 */
@RestController
@RequestMapping("/api")
public class EarnedPointsResource {

    private final Logger log = LoggerFactory.getLogger(EarnedPointsResource.class);

    @Inject
    private EarnedPointsRepository earnedPointsRepository;

    @Inject
    private MemberRepository memberRepository;

    /**
     * POST  /earnedPointss -> Create a new earnedPoints.
     */
    @RequestMapping(value = "/earnedPointss",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EarnedPoints> createEarnedPoints(@Valid @RequestBody EarnedPoints earnedPoints) throws URISyntaxException {
        log.debug("REST request to save EarnedPoints : {}", earnedPoints);
        if (earnedPoints.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("earnedPoints", "idexists", "A new earnedPoints cannot already have an ID")).body(null);
        }
        EarnedPoints result = earnedPointsRepository.save(earnedPoints);
        addVerifiedPointsToMember(earnedPoints);
        return ResponseEntity.created(new URI("/api/earnedPointss/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("earnedPoints", result.getId().toString()))
            .body(result);
    }

    /**
     * When points are verified, add them to the member who earned them.  This updates their total column for the year
     * for quick reference.
     *
     * @param earnedPoints the earned points entity.
     */
    private void addVerifiedPointsToMember(@Valid @RequestBody EarnedPoints earnedPoints) {
        // check if the entry is verified.  If it is, then also add the points to the member's total for the year.
        if (earnedPoints.getVerified()) {
            Member earner = memberRepository.findOne(earnedPoints.getMember().getId());
            earner.setCurrentYearPoints(earner.getCurrentYearPoints()+earnedPoints.getPointValue());
            memberRepository.save(earner);
        }
    }

    /**
     * When points are removed, subtract them from the member who earned them.  This updates their total column for the year
     * for quick reference.
     *
     * @param earnedPoints the earned points entity.
     */
    private void subtractPointsFromMember(@Valid @RequestBody EarnedPoints earnedPoints) {
        Member earner = memberRepository.findOne(earnedPoints.getMember().getId());
        // only subtract if the member has points for the current year - we don't allow folks to go negative
        // on points, that wouldn't make a ton of sense..
        if (earner.getCurrentYearPoints() > 0) {
            float newPointsTotal = (earner.getCurrentYearPoints() - earnedPoints.getPointValue());
            // also check to make sure when we do the subtraction that the total doesn't go below
            // zero, because that would make no sense.
            if (newPointsTotal < 0) {
                newPointsTotal = 0.0f;
            }
            earner.setCurrentYearPoints(newPointsTotal);
        }
        memberRepository.save(earner);
    }


    /**
     * PUT  /earnedPointss -> Updates an existing earnedPoints.
     */
    @RequestMapping(value = "/earnedPointss",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EarnedPoints> updateEarnedPoints(@Valid @RequestBody EarnedPoints earnedPoints) throws URISyntaxException {
        log.debug("REST request to update EarnedPoints : {}", earnedPoints);
        if (earnedPoints.getId() == null) {
            return createEarnedPoints(earnedPoints);
        }
        EarnedPoints result = earnedPointsRepository.save(earnedPoints);
        addVerifiedPointsToMember(earnedPoints);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("earnedPoints", earnedPoints.getId().toString()))
            .body(result);
    }

    /**
     * GET  /earnedPointss -> get all the earnedPointss.
     */
    @RequestMapping(value = "/earnedPointss",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<EarnedPoints>> getAllEarnedPointss(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of EarnedPointss");
        // check to see if we are an admin, and only show their own info if that is the case
        Page<EarnedPoints> page = null;
        if (SecurityUtils.isCurrentUserAdmin()) {
            page = earnedPointsRepository.findAllOrdered(pageable);
        } else {
            page = earnedPointsRepository.findForUser(pageable, SecurityUtils.getCurrentUserLogin());
        }

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/earnedPointss");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /earnedPointss/:id -> get the "id" earnedPoints.
     */
    @RequestMapping(value = "/earnedPointss/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EarnedPoints> getEarnedPoints(@PathVariable Long id) {
        log.debug("REST request to get EarnedPoints : {}", id);
        EarnedPoints earnedPoints = earnedPointsRepository.findOne(id);
        return Optional.ofNullable(earnedPoints)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /earnedPointss/:id -> delete the "id" earnedPoints.
     */
    @RequestMapping(value = "/earnedPointss/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteEarnedPoints(@PathVariable Long id) {
        log.debug("REST request to delete EarnedPoints : {}", id);
        EarnedPoints earnedPoints = earnedPointsRepository.findOne(id);
        subtractPointsFromMember(earnedPoints);
        earnedPointsRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("earnedPoints", id.toString())).build();
    }
}
