public class Transaction {
    private String transactionId;
    private String userId;
    private String bookId;
    private String dateBorrowed;
    private String dateReturned;

    public Transaction(String transactionId, String userId, String bookId, String dateBorrowed, String dateReturned) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.bookId = bookId;
        this.dateBorrowed = dateBorrowed;
        this.dateReturned = dateReturned;
    }

    public void displayTransaction() {
        System.out.println(transactionId + " | " + userId + " | " + bookId + " | " + dateBorrowed + " | " +
                (dateReturned == null || dateReturned.isEmpty() ? "Not Returned" : dateReturned));
    }

    public String toFileString() {
        return transactionId + "," + userId + "," + bookId + "," + dateBorrowed + "," +
                (dateReturned == null ? "" : dateReturned);
    }

    public boolean isReturned() {
        return dateReturned != null && !dateReturned.isEmpty();
    }

    public String getUserId() {
        return userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setDateReturned(String dateReturned) {
        this.dateReturned = dateReturned;
    }
}
