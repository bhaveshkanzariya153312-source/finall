package com.library.dao;

public class ReturnDAO {
    private final IssueDAO issueDAO = new IssueDAO();

    public boolean returnBook(int issueId) throws Exception {
        return issueDAO.returnBook(issueId);
    }
}
