package com.dogu.notification.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailTemplateService {

    private static final String BRAND_COLOR = "#6C5CE7";
    private static final String BRAND_DARK = "#0B0E1A";
    private static final String BRAND_CARD = "#141829";
    private static final String TEXT_PRIMARY = "#EEEEF0";
    private static final String TEXT_MUTED = "#9B9BB4";
    private static final String ACCENT_GREEN = "#00B894";

    private String baseLayout(String title, String content) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'>"
            + "<title>" + title + "</title></head>"
            + "<body style='margin:0;padding:0;background-color:" + BRAND_DARK + ";font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica Neue,Arial,sans-serif;'>"
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:" + BRAND_DARK + ";padding:40px 20px;'>"
            + "<tr><td align='center'>"
            + "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;'>"
            // Header
            + "<tr><td style='text-align:center;padding:30px 0;'>"
            + "<div style='display:inline-block;width:48px;height:48px;background:linear-gradient(135deg," + BRAND_COLOR + ",#a855f7);border-radius:14px;line-height:48px;font-size:24px;font-weight:800;color:white;text-align:center;'>N</div>"
            + "<div style='font-size:24px;font-weight:700;color:" + TEXT_PRIMARY + ";margin-top:12px;letter-spacing:-0.5px;'>NovaMart</div>"
            + "</td></tr>"
            // Body card
            + "<tr><td style='background-color:" + BRAND_CARD + ";border-radius:16px;border:1px solid rgba(108,92,231,0.15);padding:40px;'>"
            + content
            + "</td></tr>"
            // Footer
            + "<tr><td style='text-align:center;padding:30px 0;'>"
            + "<p style='color:" + TEXT_MUTED + ";font-size:13px;margin:0;'>© 2026 NovaMart. All rights reserved.</p>"
            + "<p style='color:" + TEXT_MUTED + ";font-size:12px;margin:8px 0 0;'>This is an automated message, please do not reply.</p>"
            + "</td></tr>"
            + "</table>"
            + "</td></tr></table>"
            + "</body></html>";
    }

    // ─── Welcome Email ───────────────────────────────────────────────

    public String buildWelcomeEmail(String name) {
        String content = ""
            + "<div style='text-align:center;margin-bottom:30px;'>"
            + "<div style='display:inline-block;width:72px;height:72px;background:linear-gradient(135deg," + BRAND_COLOR + ",#a855f7);border-radius:50%;line-height:72px;font-size:36px;text-align:center;'>&#128075;</div>"
            + "</div>"
            + "<h1 style='color:" + TEXT_PRIMARY + ";font-size:26px;text-align:center;margin:0 0 8px;font-weight:700;'>Welcome to NovaMart!</h1>"
            + "<p style='color:" + TEXT_MUTED + ";text-align:center;font-size:16px;margin:0 0 30px;'>Hey " + escapeHtml(name) + ", we're thrilled to have you on board.</p>"
            + "<div style='background:linear-gradient(135deg,rgba(108,92,231,0.12),rgba(168,85,247,0.08));border-radius:12px;padding:24px;margin-bottom:30px;border:1px solid rgba(108,92,231,0.15);'>"
            + "<p style='color:" + TEXT_PRIMARY + ";font-size:15px;margin:0 0 16px;line-height:1.6;'>Your account has been created successfully. Here's what you can do:</p>"
            + "<table width='100%' cellpadding='0' cellspacing='0'>"
            + featureRow("&#128722;", "Shop Products", "Browse our catalog and discover amazing deals")
            + featureRow("&#10084;", "Wishlist", "Save your favorite items for later")
            + featureRow("&#128230;", "Track Orders", "Follow your orders from purchase to delivery")
            + "</table>"
            + "</div>"
            + "<div style='text-align:center;'>"
            + "<a href='#' style='display:inline-block;background:linear-gradient(135deg," + BRAND_COLOR + ",#a855f7);color:white;text-decoration:none;padding:14px 40px;border-radius:10px;font-weight:600;font-size:16px;'>Start Shopping</a>"
            + "</div>";

        return baseLayout("Welcome to NovaMart", content);
    }

    private String featureRow(String icon, String title, String desc) {
        return "<tr><td style='padding:6px 0;'>"
            + "<table cellpadding='0' cellspacing='0'><tr>"
            + "<td style='width:36px;font-size:20px;vertical-align:top;padding-top:2px;'>" + icon + "</td>"
            + "<td style='padding-left:8px;'>"
            + "<span style='color:" + TEXT_PRIMARY + ";font-weight:600;font-size:14px;'>" + title + "</span>"
            + "<br><span style='color:" + TEXT_MUTED + ";font-size:13px;'>" + desc + "</span>"
            + "</td></tr></table></td></tr>";
    }

    // ─── Order Confirmation Email ────────────────────────────────────

    public static class OrderItem {
        public String productName;
        public int quantity;
        public double price;

        public OrderItem(String productName, int quantity, double price) {
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
    }

    public String buildOrderEmail(String orderId, List<OrderItem> items, double totalAmount) {
        StringBuilder itemRows = new StringBuilder();
        for (OrderItem item : items) {
            double lineTotal = item.price * item.quantity;
            itemRows.append("<tr>")
                .append("<td style='padding:12px 16px;border-bottom:1px solid rgba(108,92,231,0.1);color:").append(TEXT_PRIMARY).append(";font-size:14px;'>")
                .append(escapeHtml(item.productName)).append("</td>")
                .append("<td style='padding:12px 16px;border-bottom:1px solid rgba(108,92,231,0.1);color:").append(TEXT_MUTED).append(";font-size:14px;text-align:center;'>")
                .append(item.quantity).append("</td>")
                .append("<td style='padding:12px 16px;border-bottom:1px solid rgba(108,92,231,0.1);color:").append(TEXT_MUTED).append(";font-size:14px;text-align:right;'>")
                .append("$").append(String.format("%.2f", item.price)).append("</td>")
                .append("<td style='padding:12px 16px;border-bottom:1px solid rgba(108,92,231,0.1);color:").append(TEXT_PRIMARY).append(";font-size:14px;text-align:right;font-weight:600;'>")
                .append("$").append(String.format("%.2f", lineTotal)).append("</td>")
                .append("</tr>");
        }

        String content = ""
            + "<div style='text-align:center;margin-bottom:30px;'>"
            + "<div style='display:inline-block;width:72px;height:72px;background:linear-gradient(135deg," + ACCENT_GREEN + ",#00D2A0);border-radius:50%;line-height:72px;font-size:36px;text-align:center;'>&#10003;</div>"
            + "</div>"
            + "<h1 style='color:" + TEXT_PRIMARY + ";font-size:26px;text-align:center;margin:0 0 8px;font-weight:700;'>Order Confirmed!</h1>"
            + "<p style='color:" + TEXT_MUTED + ";text-align:center;font-size:15px;margin:0 0 30px;'>Your order has been placed successfully.</p>"
            // Order info badge
            + "<div style='text-align:center;margin-bottom:24px;'>"
            + "<span style='display:inline-block;background:rgba(108,92,231,0.15);color:" + BRAND_COLOR + ";padding:8px 20px;border-radius:20px;font-size:13px;font-weight:600;letter-spacing:0.5px;'>ORDER " + escapeHtml(orderId.length() > 12 ? orderId.substring(0, 12) + "..." : orderId) + "</span>"
            + "</div>"
            // Items table
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background:rgba(0,0,0,0.2);border-radius:12px;overflow:hidden;margin-bottom:24px;'>"
            + "<tr>"
            + "<th style='padding:14px 16px;text-align:left;color:" + TEXT_MUTED + ";font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;border-bottom:1px solid rgba(108,92,231,0.15);'>Product</th>"
            + "<th style='padding:14px 16px;text-align:center;color:" + TEXT_MUTED + ";font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;border-bottom:1px solid rgba(108,92,231,0.15);'>Qty</th>"
            + "<th style='padding:14px 16px;text-align:right;color:" + TEXT_MUTED + ";font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;border-bottom:1px solid rgba(108,92,231,0.15);'>Price</th>"
            + "<th style='padding:14px 16px;text-align:right;color:" + TEXT_MUTED + ";font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;border-bottom:1px solid rgba(108,92,231,0.15);'>Total</th>"
            + "</tr>"
            + itemRows
            + "</table>"
            // Total
            + "<div style='text-align:right;padding:16px 20px;background:linear-gradient(135deg,rgba(108,92,231,0.12),rgba(168,85,247,0.08));border-radius:12px;border:1px solid rgba(108,92,231,0.15);'>"
            + "<span style='color:" + TEXT_MUTED + ";font-size:14px;'>Total Amount: </span>"
            + "<span style='color:" + TEXT_PRIMARY + ";font-size:24px;font-weight:700;margin-left:8px;'>$" + String.format("%.2f", totalAmount) + "</span>"
            + "</div>"
            + "<p style='color:" + TEXT_MUTED + ";font-size:14px;text-align:center;margin:24px 0 0;line-height:1.5;'>We'll send you another email when your invoice is ready.</p>";

        return baseLayout("Order Confirmation", content);
    }

    // ─── Invoice Email ───────────────────────────────────────────────

    public String buildInvoiceEmail(String invoiceSlug, List<OrderItem> items, double totalAmount) {
        StringBuilder itemRows = new StringBuilder();
        for (OrderItem item : items) {
            double lineTotal = item.price * item.quantity;
            itemRows.append("<tr>")
                .append("<td style='padding:12px 16px;border-bottom:1px solid rgba(108,92,231,0.1);color:").append(TEXT_PRIMARY).append(";font-size:14px;'>")
                .append(escapeHtml(item.productName)).append("</td>")
                .append("<td style='padding:12px 16px;border-bottom:1px solid rgba(108,92,231,0.1);color:").append(TEXT_MUTED).append(";font-size:14px;text-align:center;'>")
                .append(item.quantity).append("</td>")
                .append("<td style='padding:12px 16px;border-bottom:1px solid rgba(108,92,231,0.1);color:").append(TEXT_MUTED).append(";font-size:14px;text-align:right;'>")
                .append("$").append(String.format("%.2f", item.price)).append("</td>")
                .append("<td style='padding:12px 16px;border-bottom:1px solid rgba(108,92,231,0.1);color:").append(TEXT_PRIMARY).append(";font-size:14px;text-align:right;font-weight:600;'>")
                .append("$").append(String.format("%.2f", lineTotal)).append("</td>")
                .append("</tr>");
        }

        String content = ""
            + "<div style='text-align:center;margin-bottom:30px;'>"
            + "<div style='display:inline-block;width:72px;height:72px;background:linear-gradient(135deg,#0984E3,#74B9FF);border-radius:50%;line-height:72px;font-size:36px;text-align:center;'>&#128196;</div>"
            + "</div>"
            + "<h1 style='color:" + TEXT_PRIMARY + ";font-size:26px;text-align:center;margin:0 0 8px;font-weight:700;'>Invoice Generated</h1>"
            + "<p style='color:" + TEXT_MUTED + ";text-align:center;font-size:15px;margin:0 0 30px;'>Here's the invoice for your recent order.</p>"
            // Invoice slug badge
            + "<div style='text-align:center;margin-bottom:24px;'>"
            + "<span style='display:inline-block;background:rgba(9,132,227,0.15);color:#74B9FF;padding:8px 20px;border-radius:20px;font-size:13px;font-weight:600;letter-spacing:0.5px;'>" + escapeHtml(invoiceSlug) + "</span>"
            + "</div>"
            // Items table
            + "<table width='100%' cellpadding='0' cellspacing='0' style='background:rgba(0,0,0,0.2);border-radius:12px;overflow:hidden;margin-bottom:24px;'>"
            + "<tr>"
            + "<th style='padding:14px 16px;text-align:left;color:" + TEXT_MUTED + ";font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;border-bottom:1px solid rgba(108,92,231,0.15);'>Product</th>"
            + "<th style='padding:14px 16px;text-align:center;color:" + TEXT_MUTED + ";font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;border-bottom:1px solid rgba(108,92,231,0.15);'>Qty</th>"
            + "<th style='padding:14px 16px;text-align:right;color:" + TEXT_MUTED + ";font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;border-bottom:1px solid rgba(108,92,231,0.15);'>Unit Price</th>"
            + "<th style='padding:14px 16px;text-align:right;color:" + TEXT_MUTED + ";font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;border-bottom:1px solid rgba(108,92,231,0.15);'>Total</th>"
            + "</tr>"
            + itemRows
            + "</table>"
            // Total
            + "<div style='text-align:right;padding:16px 20px;background:linear-gradient(135deg,rgba(9,132,227,0.12),rgba(116,185,255,0.08));border-radius:12px;border:1px solid rgba(9,132,227,0.15);'>"
            + "<span style='color:" + TEXT_MUTED + ";font-size:14px;'>Total Amount: </span>"
            + "<span style='color:" + TEXT_PRIMARY + ";font-size:24px;font-weight:700;margin-left:8px;'>$" + String.format("%.2f", totalAmount) + "</span>"
            + "</div>"
            + "<p style='color:" + TEXT_MUTED + ";font-size:14px;text-align:center;margin:24px 0 0;line-height:1.5;'>Thank you for shopping with NovaMart!</p>";

        return baseLayout("Invoice " + invoiceSlug, content);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
