package com.billwise.service;

import com.billwise.model.Invoice;
import com.billwise.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> getInvoicesByCreator(String searchQuery) {
        return invoiceRepository.findByCreator(searchQuery);
    }

    public long getInvoiceCountByCreator(String searchQuery) {
        return invoiceRepository.countByCreator(searchQuery);
    }

    public Invoice getInvoiceById(String id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getCreatedAt() == null) {
            invoice.setCreatedAt(new Date());
        }
        if (invoice.getStatus() == null) {
            invoice.setStatus("Unpaid");
        }
        if (invoice.getTotalAmountReceived() == null) {
            invoice.setTotalAmountReceived(0.0);
        }
        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(String id, Invoice invoiceUpdate) {
        Invoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));

        if (invoiceUpdate.getDueDate() != null) existing.setDueDate(invoiceUpdate.getDueDate());
        if (invoiceUpdate.getCurrency() != null) existing.setCurrency(invoiceUpdate.getCurrency());
        if (invoiceUpdate.getItems() != null) existing.setItems(invoiceUpdate.getItems());
        if (invoiceUpdate.getRates() != null) existing.setRates(invoiceUpdate.getRates());
        if (invoiceUpdate.getVat() != null) existing.setVat(invoiceUpdate.getVat());
        if (invoiceUpdate.getTotal() != null) existing.setTotal(invoiceUpdate.getTotal());
        if (invoiceUpdate.getSubTotal() != null) existing.setSubTotal(invoiceUpdate.getSubTotal());
        if (invoiceUpdate.getNotes() != null) existing.setNotes(invoiceUpdate.getNotes());
        if (invoiceUpdate.getStatus() != null) existing.setStatus(invoiceUpdate.getStatus());
        if (invoiceUpdate.getInvoiceNumber() != null) existing.setInvoiceNumber(invoiceUpdate.getInvoiceNumber());
        if (invoiceUpdate.getType() != null) existing.setType(invoiceUpdate.getType());
        if (invoiceUpdate.getCreator() != null) existing.setCreator(invoiceUpdate.getCreator());
        if (invoiceUpdate.getTotalAmountReceived() != null) existing.setTotalAmountReceived(invoiceUpdate.getTotalAmountReceived());
        if (invoiceUpdate.getClient() != null) existing.setClient(invoiceUpdate.getClient());
        if (invoiceUpdate.getPaymentRecords() != null) existing.setPaymentRecords(invoiceUpdate.getPaymentRecords());

        return invoiceRepository.save(existing);
    }

    public void deleteInvoice(String id) {
        if (!invoiceRepository.existsById(id)) {
            throw new RuntimeException("Invoice not found with id: " + id);
        }
        invoiceRepository.deleteById(id);
    }
}
