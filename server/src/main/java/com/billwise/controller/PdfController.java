package com.billwise.controller;

import com.billwise.service.EmailService;
import com.billwise.service.PdfService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import com.billwise.service.InvoiceService;
import com.billwise.service.ProfileService;
import com.billwise.model.Invoice;
import com.billwise.model.Profile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@RestController
public class PdfController {

    private final PdfService pdfService;
    private final EmailService emailService;
    private final InvoiceService invoiceService;
    private final ProfileService profileService;

    @Value("${react.app.url}")
    private String reactAppUrl;

    @Value("${server.port:5000}")
    private String serverPort;

    // Temporary storage for generated PDFs
    private final ConcurrentHashMap<String, byte[]> pdfStore = new ConcurrentHashMap<>();

    public PdfController(PdfService pdfService, EmailService emailService, 
                         InvoiceService invoiceService, ProfileService profileService) {
        this.pdfService = pdfService;
        this.emailService = emailService;
        this.invoiceService = invoiceService;
        this.profileService = profileService;
    }

    @PostMapping("/send-pdf")
    public ResponseEntity<?> sendPdf(@RequestBody Map<String, Object> requestData) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> invoiceData = (Map<String, Object>) requestData.get("invoiceData");
            String recipientEmail = (String) requestData.get("email");
            String companyName = (String) requestData.getOrDefault("company", "BillWise");
            String invoiceNumber = (String) requestData.getOrDefault("invoiceNumber", "INV-001");
            Object balanceObj = requestData.getOrDefault("balance", 0);
            String currency = (String) requestData.getOrDefault("currency", "USD");
            String link = (String) requestData.getOrDefault("link", reactAppUrl);

            if (recipientEmail == null || recipientEmail.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Recipient email is required"));
            }

            // Flatten the payload so the Thymeleaf template can read root-level variables
            Map<String, Object> flatData = flattenPdfData(requestData);

            // Generate PDF
            File pdfFile = pdfService.generatePdfFile(flatData);

            // Build email HTML
            String emailHtml = buildInvoiceEmailHtml(companyName, invoiceNumber, balanceObj, currency, link);

            // Extract sender email from profile (defaults to BillWise system if absent)
            String senderEmail = flatData.get("companyEmail") != null ? 
                                 (String) flatData.get("companyEmail") : "noreply@billwise.com";

            // Send email with PDF attachment
            emailService.sendEmailWithAttachment(
                    senderEmail,
                    recipientEmail,
                    "Invoice from " + companyName + " - " + invoiceNumber,
                    emailHtml,
                    pdfFile,
                    "invoice-" + invoiceNumber + ".pdf"
            );

            // Clean up temp file
            pdfFile.delete();

            return ResponseEntity.ok(Map.of("message", "Invoice sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to send PDF: " + e.getMessage()));
        }
    }

    @PostMapping("/create-pdf")
    public ResponseEntity<?> createPdf(@RequestBody Map<String, Object> requestData) {
        try {
            Map<String, Object> flatData = flattenPdfData(requestData);
            byte[] pdfBytes = pdfService.generatePdf(flatData);

            // Store with a simple key
            String key = "latest";
            pdfStore.put(key, pdfBytes);

            return ResponseEntity.ok(Map.of("message", "PDF created successfully", "key", key));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to create PDF: " + e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> flattenPdfData(Map<String, Object> requestData) {
        Map<String, Object> flat = new java.util.HashMap<>();

        // Extract invoice data
        Map<String, Object> invoiceData = requestData.get("invoiceData") instanceof Map
                ? (Map<String, Object>) requestData.get("invoiceData")
                : requestData;

        // Invoice fields
        flat.put("invoiceNumber", invoiceData.get("invoiceNumber"));
        flat.put("type", invoiceData.get("type"));
        flat.put("status", invoiceData.get("status"));
        flat.put("currency", invoiceData.get("currency"));
        flat.put("dueDate", formatDate(invoiceData.get("dueDate")));
        flat.put("createdAt", formatDate(invoiceData.get("createdAt")));
        flat.put("items", invoiceData.get("items"));
        flat.put("vat", invoiceData.get("vat"));
        flat.put("total", invoiceData.get("total"));
        flat.put("subTotal", invoiceData.get("subTotal"));
        flat.put("notes", invoiceData.get("notes"));
        flat.put("totalAmountReceived", invoiceData.get("totalAmountReceived"));
        flat.put("rates", invoiceData.get("rates"));

        // Client info
        Object clientObj = invoiceData.get("client");
        if (clientObj instanceof Map) {
            Map<String, Object> client = (Map<String, Object>) clientObj;
            flat.put("clientName", client.get("name"));
            flat.put("clientEmail", client.get("email"));
            flat.put("clientPhone", client.get("phone"));
            flat.put("clientAddress", client.get("address"));
        }

        // Profile / Company info
        Map<String, Object> profileData = requestData.get("profileData") instanceof Map
                ? (Map<String, Object>) requestData.get("profileData")
                : null;
        if (profileData != null) {
            flat.put("companyName", profileData.getOrDefault("businessName",
                    profileData.getOrDefault("name", requestData.get("company"))));
            flat.put("companyEmail", profileData.get("email"));
            flat.put("companyPhone", profileData.get("phoneNumber"));
            flat.put("companyAddress", profileData.get("contactAddress"));
            flat.put("companyWebsite", profileData.get("website"));
        } else {
            flat.put("companyName", requestData.getOrDefault("company", "BillWise"));
        }

        return flat;
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "---";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            if (dateObj instanceof Long) {
                return sdf.format(new Date((Long) dateObj));
            } else if (dateObj instanceof Integer) {
                return sdf.format(new Date(((Integer) dateObj).longValue()));
            } else if (dateObj instanceof String) {
                String dateStr = (String) dateObj;
                try {
                    // Try parsing ISO date
                    Date parsed = Date.from(Instant.parse(dateStr));
                    return sdf.format(parsed);
                } catch (Exception e) {
                    // Fallback to returned string as-is
                    return dateStr.substring(0, Math.min(dateStr.length(), 10));
                }
            } else if (dateObj instanceof Date) {
                return sdf.format((Date) dateObj);
            }
        } catch (Exception ignored) {}
        return dateObj.toString();
    }

    @GetMapping("/fetch-pdf")
    public ResponseEntity<?> fetchPdf() {
        try {
            byte[] pdfBytes = pdfStore.get("latest");
            if (pdfBytes == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No PDF found. Create one first."));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice.pdf\"");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch PDF: " + e.getMessage()));
        }
    }

    @GetMapping("/public/pdf/{id}")
    public ResponseEntity<?> getPublicPdf(@PathVariable String id) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(id);
            String creatorId = invoice.getCreator() != null ? invoice.getCreator().toString() : "";
            
            Profile profile = null;
            if (!creatorId.isEmpty()) {
                List<Profile> profiles = profileService.getProfilesByUser(creatorId);
                if (!profiles.isEmpty()) {
                    profile = profiles.get(0);
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            Map<String, Object> reqInvoice = mapper.convertValue(invoice, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> reqProfile = profile != null ? mapper.convertValue(profile, new TypeReference<Map<String, Object>>() {}) : null;

            Map<String, Object> requestData = new HashMap<>();
            requestData.put("invoiceData", reqInvoice);
            if (reqProfile != null) {
                requestData.put("profileData", reqProfile);
            }

            Map<String, Object> flatData = flattenPdfData(requestData);
            byte[] pdfBytes = pdfService.generatePdf(flatData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"invoice-" + invoice.getInvoiceNumber() + ".pdf\"");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch public PDF: " + e.getMessage()));
        }
    }

    private String buildInvoiceEmailHtml(String companyName, String invoiceNumber,
                                          Object balance, String currency, String link) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f0f2f5; margin: 0; padding: 0; }
                    .wrapper { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .card { background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
                    .header { background: linear-gradient(135deg, #1976d2, #1565c0); padding: 32px; text-align: center; }
                    .header h1 { color: #ffffff; margin: 0; font-size: 28px; letter-spacing: 1px; }
                    .header p { color: rgba(255,255,255,0.85); margin: 8px 0 0; font-size: 14px; }
                    .body-content { padding: 32px; }
                    .invoice-badge { display: inline-block; background: #e3f2fd; color: #1976d2; padding: 6px 16px;
                                     border-radius: 20px; font-size: 13px; font-weight: 600; margin-bottom: 20px; }
                    .amount-box { background: #f8f9fa; border-radius: 8px; padding: 24px; text-align: center; margin: 24px 0; }
                    .amount-label { color: #666; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; }
                    .amount-value { color: #1976d2; font-size: 36px; font-weight: 700; margin: 8px 0; }
                    .btn { display: inline-block; background: #1976d2; color: #ffffff; padding: 14px 32px;
                           text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 15px; }
                    .btn:hover { background: #1565c0; }
                    .footer { text-align: center; padding: 24px; color: #999; font-size: 12px; }
                    .divider { border: none; border-top: 1px solid #eee; margin: 24px 0; }
                </style>
            </head>
            <body>
                <div class="wrapper">
                    <div class="card">
                        <div class="header">
                            <h1>%s</h1>
                            <p>Invoice Notification</p>
                        </div>
                        <div class="body-content">
                            <span class="invoice-badge">Invoice %s</span>
                            <p style="color: #333; line-height: 1.6;">
                                You have received an invoice. Please find the details below and the PDF attached to this email.
                            </p>
                            <div class="amount-box">
                                <div class="amount-label">Balance Due</div>
                                <div class="amount-value">%s %s</div>
                            </div>
                            <hr class="divider">
                            <p style="text-align: center; margin: 28px 0;">
                                <a href="%s" class="btn">View Invoice Online</a>
                            </p>
                            <p style="color: #888; font-size: 13px; text-align: center;">
                                The invoice PDF is attached to this email for your records.
                            </p>
                        </div>
                        <div class="footer">
                            <p>This email was sent by %s via BillWise.</p>
                            <p>&copy; BillWise. All rights reserved.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(companyName, invoiceNumber, currency, balance, link, companyName);
    }
}
