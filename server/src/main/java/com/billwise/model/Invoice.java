package com.billwise.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "invoices")
public class Invoice {

    @Id
    @JsonProperty("_id")
    private String id;

    private Date dueDate;

    private String currency;

    private List<InvoiceItem> items = new ArrayList<>();

    private String rates;

    private Double vat;

    private Double total;

    private Double subTotal;

    private String notes;

    private String status = "Unpaid";

    private String invoiceNumber;

    private String type;

    private Object creator;

    private Double totalAmountReceived = 0.0;

    private ClientInfo client;

    private List<PaymentRecord> paymentRecords = new ArrayList<>();

    private Date createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItem {
        private String itemName;
        private Double unitPrice;
        private Integer quantity;
        private String discount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClientInfo {
        private String name;
        private String email;
        private String phone;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRecord {
        private Double amountPaid;
        private Date datePaid;
        private String paymentMethod;
        private String note;
        private String paidBy;
    }
}
