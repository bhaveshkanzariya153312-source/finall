package com.library.model;

public class Fine {
    private int issueId;
    private int studentId;
    private String studentName;
    private String bookTitle;
    private String dueDate;
    private int days;
    private double amount;

    public int getIssueId() { return issueId; }
    public void setIssueId(int issueId) { this.issueId = issueId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
