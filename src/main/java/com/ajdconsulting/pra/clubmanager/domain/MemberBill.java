package com.ajdconsulting.pra.clubmanager.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="member_bill")
public class MemberBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="generated_date")
    private LocalDateTime generatedDate;
    @Column(name="year")
    private int year;
    @Column(name="amount")
    private double amount;
    @Column(name="amount_with_fee")
    private double amountWithFee;
    @JoinColumn(name = "member_id")
    @OneToOne
    private Member member;
    @Column(name="emailed_bill")
    private String emailedBill;
    @Column(name="sent")
    private boolean isSent;

    public MemberBill() {

    }

    public MemberBill(Member member) {
        generatedDate = LocalDateTime.now();
        this.member = member;
        // a new bill will never be sent, the send job will set that attribute
        this.isSent = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        this.amountWithFee = (amount*1.03);
        if (amountWithFee > 0) {
            amountWithFee += 0.25;
        }
    }

    public double getAmountWithFee() {
        return amountWithFee;
    }

    public String getEmailedBill() {
        return emailedBill;
    }

    public void setEmailedBill(String billContent) {
        emailedBill = billContent;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member m) {
        this.member = m;
    }

    public boolean getSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        this.isSent = sent;
    }
}
