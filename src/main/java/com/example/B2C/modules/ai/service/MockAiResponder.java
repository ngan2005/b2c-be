package com.example.B2C.modules.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides mock responses for AI endpoints when {@code OPENAI_API_KEY} is not
 * configured. Keeps the service contract identical so callers don't need to
 * branch on AI availability.
 */
@Slf4j
@Component
public class MockAiResponder {

    private final AtomicInteger chatCounter = new AtomicInteger(0);

    public String chatReply(String userMessage) {
        int n = chatCounter.incrementAndGet();
        if (userMessage == null || userMessage.isBlank()) {
            return "[MOCK AI] Ban chua nhap noi dung. Hay mo ta san pham ban dang tim nhe!";
        }
        String lower = userMessage.toLowerCase();
        if (lower.contains("xin chao") || lower.contains("hello") || lower.contains("hi ")) {
            return "[MOCK AI #" + n + "] Xin chao! Toi la tro ly ao B2C (che do mock). " +
                    "Ban dang tim san pham gi? Vd: dien thoai, laptop, thoi trang, gia dung...";
        }
        if (lower.contains("dien thoai") || lower.contains("phone")) {
            return "[MOCK AI #" + n + "] Goi y cho ban: cac mau smartphone ban chay tren san B2C " +
                    "dang duoc giam gia 15-25%. Ban quan tam hang nao (Samsung, iPhone, Xiaomi)?";
        }
        if (lower.contains("laptop")) {
            return "[MOCK AI #" + n + "] Laptop phu hop voi ban tam 15-20 trieu cho cong viec van phong. " +
                    "Ban can dung cho muc dich gi (hoc tap, lam viec, gaming)?";
        }
        return "[MOCK AI #" + n + "] Cam on ban da hoi ve: \"" + truncate(userMessage, 60) + "\". " +
                "(Day la phan hoi mau - de nhan cau tra loi that, hay cau hinh OPENAI_API_KEY.)";
    }

    public String productDescription(String productName, String category, String brand,
                                     java.util.List<String> features, String audience, String tone) {
        StringBuilder sb = new StringBuilder();
        sb.append("[MOCK AI - Product Description]\n\n");
        sb.append("🔹 ").append(productName == null || productName.isBlank() ? "San pham" : productName).append("\n\n");
        sb.append("Kham pha ").append(productName == null || productName.isBlank() ? "san pham tuyet voi nay" : productName);
        if (brand != null && !brand.isBlank()) sb.append(" den tu thuong hieu ").append(brand);
        sb.append(" - lua chon hoan hao cho ").append(audience == null || audience.isBlank() ? "moi khach hang" : audience);
        sb.append(".\n\n");
        if (category != null && !category.isBlank()) {
            sb.append("📂 Danh muc: ").append(category).append("\n");
        }
        if (features != null && !features.isEmpty()) {
            sb.append("✨ Tinh nang noi bat:\n");
            for (String f : features) {
                sb.append("  • ").append(f.trim()).append("\n");
            }
            sb.append("\n");
        }
        sb.append("🎯 Phong cach: ").append(tone == null || tone.isBlank() ? "chuyen nghiep" : tone).append("\n\n");
        sb.append("👉 Dat hang ngay hom nay de nhan uu dai hap dan!\n\n");
        sb.append("---\n");
        sb.append("(Day la mo ta mau duoc tao boi Mock AI. De nhan noi dung SEO that, cau hinh OPENAI_API_KEY.)");
        return sb.toString();
    }

    public String newConversationId() {
        return UUID.randomUUID().toString();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
