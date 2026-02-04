import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class IntegratedChatApp {
    private static SellerWindow sellerWindow;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            sellerWindow = new SellerWindow();
            new BuyerChatWindow(sellerWindow);
        });
    }
}

// =============================== 
// CHAT BLOCK PATTERN INTERFACES
// =============================== 

// Base interface for all chat blocks
interface ChatBlock {
    JPanel createComponent();
    String getType();
}

// MessageBlock: hanya menampilkan pesan
class MessageBlock implements ChatBlock {
    private String message;
    private boolean isUser;
    
    public MessageBlock(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }
    
    @Override
    public JPanel createComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(252, 252, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        JLabel label = new JLabel("<html><div style='padding: 8px 12px; border-radius: 12px; "
                + (isUser ? "background: #007AFF; color: white;" : "background: #E4E6EB; color: black;") 
                + "'>" + message + "</div></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        if (isUser) {
            panel.add(label, BorderLayout.EAST);
        } else {
            panel.add(label, BorderLayout.WEST);
        }
        
        return panel;
    }
    
    @Override
    public String getType() {
        return "MESSAGE";
    }
    
    public String getMessage() { return message; }
    public boolean isUser() { return isUser; }
}

// RowBlock: menampilkan rows (grid) item
class RowBlock implements ChatBlock {
    private List<MenuItem> items;
    private BuyerChatWindow buyerWindow;
    
    public RowBlock(List<MenuItem> items, BuyerChatWindow buyerWindow) {
        this.items = items;
        this.buyerWindow = buyerWindow;
    }
    
    @Override
    public JPanel createComponent() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 15));
        panel.setBackground(new Color(252, 252, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        for (MenuItem item : items) {
            panel.add(createItemCard(item));
        }
        
        return panel;
    }
    
    private JPanel createItemCard(MenuItem item) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Item info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel priceLabel = new JLabel("Rp " + String.format("%,d", item.getPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priceLabel.setForeground(new Color(100, 100, 100));
        
        JLabel categoryLabel = new JLabel(item.getCategory().toUpperCase());
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        categoryLabel.setForeground(Color.GRAY);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(categoryLabel);

        // Add controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setOpaque(false);
        
        // Quantity selector
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        qtyPanel.setOpaque(false);
        
        JLabel qtyLabel = new JLabel("Qty:");
        qtyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        qtySpinner.setPreferredSize(new Dimension(60, 25));
        ((JSpinner.DefaultEditor) qtySpinner.getEditor()).getTextField()
            .setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        qtyPanel.add(qtyLabel);
        qtyPanel.add(qtySpinner);
        
        controlPanel.add(qtyPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Add to cart button
        JButton addBtn = new JButton("+ Add to Cart");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.setBackground(new Color(100, 150, 250));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> {
            int qty = (Integer) qtySpinner.getValue();
            buyerWindow.addToCart(item, qty);
            qtySpinner.setValue(1);
        });
        
        controlPanel.add(addBtn);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(controlPanel, BorderLayout.EAST);
        
        return card;
    }
    
    @Override
    public String getType() {
        return "ROW";
    }
    
    public List<MenuItem> getItems() { return items; }
}

// ComboRowBlock: khusus untuk menampilkan combo offers
class ComboRowBlock implements ChatBlock {
    private List<ComboOffer> combos;
    private BuyerChatWindow buyerWindow;
    
    public ComboRowBlock(List<ComboOffer> combos, BuyerChatWindow buyerWindow) {
        this.combos = combos;
        this.buyerWindow = buyerWindow;
    }
    
    @Override
    public JPanel createComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(252, 252, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        for (ComboOffer combo : combos) {
            panel.add(createComboCard(combo));
            panel.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        
        return panel;
    }
    
    private JPanel createComboCard(ComboOffer combo) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(new Color(255, 250, 240));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(250, 200, 100), 2),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(combo.getItem1().getName() + " + " + combo.getItem2().getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        JLabel savingsLabel = new JLabel(String.format("Save Rp %,d!", combo.getSavings()));
        savingsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        savingsLabel.setForeground(new Color(200, 100, 50));
        
        JLabel priceLabel = new JLabel(String.format(
            "<html><s>Rp %,d</s> → <b>Rp %,d</b></html>", 
            combo.getOriginalPrice(), combo.getComboPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(savingsLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(priceLabel);

        JButton addComboBtn = new JButton("🎁 Add Combo");
        addComboBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addComboBtn.setBackground(new Color(250, 150, 50));
        addComboBtn.setForeground(Color.WHITE);
        addComboBtn.setFocusPainted(false);
        addComboBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addComboBtn.addActionListener(e -> {
            buyerWindow.addComboToCart(combo);
        });

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(addComboBtn, BorderLayout.EAST);
        
        return card;
    }
    
    @Override
    public String getType() {
        return "COMBO_ROW";
    }
    
    public List<ComboOffer> getCombos() { return combos; }
}

// MnR (Message and Rows): menampilkan pesan DAN rows
class MnRBlock implements ChatBlock {
    private String message;
    private List<MenuItem> items;
    private List<ComboOffer> combos;
    private BuyerChatWindow buyerWindow;
    private Set<String> detectedTags;
    private String category;
    
    public MnRBlock(String message, List<MenuItem> items, List<ComboOffer> combos, 
                   Set<String> detectedTags, String category, BuyerChatWindow buyerWindow) {
        this.message = message;
        this.items = items;
        this.combos = combos;
        this.detectedTags = detectedTags;
        this.category = category;
        this.buyerWindow = buyerWindow;
    }
    
    @Override
    public JPanel createComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 20, 30));
        
        // 1. Message bagian
        JPanel messagePanel = createMessagePanel();
        panel.add(messagePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // 2. Row bagian (items)
        if (!items.isEmpty()) {
            RowBlock rowBlock = new RowBlock(items, buyerWindow);
            panel.add(rowBlock.createComponent());
        }
        
        // 3. Combo rows bagian (jika ada)
        if (!combos.isEmpty()) {
            panel.add(Box.createRigidArea(new Dimension(0, 25)));
            
            JLabel comboHeader = new JLabel("💡 Combo Deals - Special Offers!");
            comboHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
            comboHeader.setForeground(new Color(200, 100, 50));
            comboHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(comboHeader);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
            
            ComboRowBlock comboRowBlock = new ComboRowBlock(combos, buyerWindow);
            panel.add(comboRowBlock.createComponent());
        }
        
        return panel;
    }
    
    private JPanel createMessagePanel() {
        JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messagePanel.setOpaque(false);
        messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel header = new JLabel(message);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        messagePanel.add(header);
        
        if (detectedTags != null && !detectedTags.isEmpty()) {
            JLabel tagsLabel = new JLabel("(" + String.join(", ", detectedTags) + ")");
            tagsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            tagsLabel.setForeground(Color.GRAY);
            messagePanel.add(tagsLabel);
        }
        
        if (category != null) {
            JLabel catLabel = new JLabel(" [" + category.toUpperCase() + "]");
            catLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            catLabel.setForeground(new Color(100, 100, 200));
            messagePanel.add(catLabel);
        }
        
        return messagePanel;
    }
    
    @Override
    public String getType() {
        return "MNR";
    }
    
    public String getMessage() { return message; }
    public List<MenuItem> getItems() { return items; }
    public List<ComboOffer> getCombos() { return combos; }
}

// =============================== 
// DATA MODELS (SAMA)
// =============================== 

class MenuItem {
    private String id;
    private String name;
    private int price;
    private Set<String> tags;
    private String category;

    public MenuItem(String id, String name, int price, String category, String... tags) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.tags = new HashSet<>(Arrays.asList(tags));
    }

    public int getMatchScore(Set<String> queryTags) {
        int score = 0;
        for (String tag : queryTags) {
            if (this.tags.contains(tag)) {
                score += 10;
            }
        }
        return score;
    }

    public boolean hasAllTags(Set<String> requiredTags) {
        return this.tags.containsAll(requiredTags);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getCategory() { return category; }
    public Set<String> getTags() { return tags; }
}

class CartItem {
    private MenuItem menuItem;
    private int quantity;
    private int itemTotal;

    public CartItem(MenuItem item, int quantity) {
        this.menuItem = item;
        this.quantity = quantity;
        this.itemTotal = item.getPrice() * quantity;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }
    public int getItemTotal() { return itemTotal; }
    
    public void setQuantity(int qty) {
        this.quantity = qty;
        this.itemTotal = menuItem.getPrice() * qty;
    }
    
    public void incrementQuantity() {
        this.quantity++;
        this.itemTotal = menuItem.getPrice() * quantity;
    }
}

class ComboOffer {
    private MenuItem item1;
    private MenuItem item2;
    private int originalPrice;
    private int comboPrice;
    private int savings;
    private String comboId;

    public ComboOffer(MenuItem item1, MenuItem item2, double discountPercent) {
        this.item1 = item1;
        this.item2 = item2;
        this.originalPrice = item1.getPrice() + item2.getPrice();
        this.comboPrice = (int)(originalPrice * (1 - discountPercent));
        this.savings = originalPrice - comboPrice;
        this.comboId = item1.getId() + "-" + item2.getId();
    }

    public MenuItem getItem1() { return item1; }
    public MenuItem getItem2() { return item2; }
    public int getComboPrice() { return comboPrice; }
    public int getSavings() { return savings; }
    public int getOriginalPrice() { return originalPrice; }
    public String getComboId() { return comboId; }
}

class AppliedCombo {
    private ComboOffer combo;
    private int timesApplied;
    
    public AppliedCombo(ComboOffer combo, int times) {
        this.combo = combo;
        this.timesApplied = times;
    }
    
    public ComboOffer getCombo() { return combo; }
    public int getTimesApplied() { return timesApplied; }
    public int getTotalSavings() { return combo.getSavings() * timesApplied; }
}

class Order {
    private String orderId;
    private String customerName;
    private String phoneNumber;
    private String deliveryAddress;
    private String specialNotes;
    private List<CartItem> items;
    private List<AppliedCombo> appliedCombos;
    private int subtotal;
    private int discount;
    private int total;
    private LocalDateTime orderTime;
    private OrderStatus status;
    
    public Order(String customerName, String phone, String address, String notes,
                 List<CartItem> items, List<AppliedCombo> combos, 
                 int subtotal, int discount, int total) {
        this.orderId = generateOrderId();
        this.customerName = customerName;
        this.phoneNumber = phone;
        this.deliveryAddress = address;
        this.specialNotes = notes;
        this.items = new ArrayList<>(items);
        this.appliedCombos = new ArrayList<>(combos);
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.orderTime = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }
    
    private String generateOrderId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return "ORD-" + LocalDateTime.now().format(formatter);
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getSpecialNotes() { return specialNotes; }
    public List<CartItem> getItems() { return items; }
    public List<AppliedCombo> getAppliedCombos() { return appliedCombos; }
    public int getSubtotal() { return subtotal; }
    public int getDiscount() { return discount; }
    public int getTotal() { return total; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return orderTime.format(formatter);
    }
}

enum OrderStatus {
    PENDING("Pending", new Color(255, 200, 100)),
    CONFIRMED("Confirmed", new Color(100, 150, 250)),
    PREPARING("Preparing", new Color(150, 100, 250)),
    READY("Ready", new Color(100, 200, 100)),
    COMPLETED("Completed", new Color(100, 200, 100)),
    REJECTED("Rejected", new Color(200, 100, 100));
    
    private String displayName;
    private Color color;
    
    OrderStatus(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() { return displayName; }
    public Color getColor() { return color; }
}

// =============================== 
// SHOPPING CART SYSTEM (SAMA)
// =============================== 

class ShoppingCart {
    private List<CartItem> items;
    private List<AppliedCombo> appliedCombos;
    private MenuRecommendationSystem menuSystem;
    
    public ShoppingCart(MenuRecommendationSystem menuSystem) {
        this.items = new ArrayList<>();
        this.appliedCombos = new ArrayList<>();
        this.menuSystem = menuSystem;
    }
    
    public void addItem(MenuItem item, int quantity) {
        for (CartItem cartItem : items) {
            if (cartItem.getMenuItem().getId().equals(item.getId())) {
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                detectAndApplyCombos();
                return;
            }
        }
        
        items.add(new CartItem(item, quantity));
        detectAndApplyCombos();
    }
    
    public void removeItem(String itemId) {
        items.removeIf(item -> item.getMenuItem().getId().equals(itemId));
        detectAndApplyCombos();
    }
    
    public void updateQuantity(String itemId, int newQty) {
        if (newQty <= 0) {
            removeItem(itemId);
            return;
        }
        
        for (CartItem item : items) {
            if (item.getMenuItem().getId().equals(itemId)) {
                item.setQuantity(newQty);
                break;
            }
        }
        detectAndApplyCombos();
    }
    
    public void detectAndApplyCombos() {
        appliedCombos.clear();
        
        if (items.size() < 2) return;
        
        List<ComboOffer> availableCombos = menuSystem.getAllCombos();
        Map<String, Integer> itemQuantities = new HashMap<>();
        for (CartItem item : items) {
            itemQuantities.put(item.getMenuItem().getId(), item.getQuantity());
        }
        
        availableCombos.sort((c1, c2) -> Integer.compare(c2.getSavings(), c1.getSavings()));
        Map<String, Integer> usedQuantities = new HashMap<>();
        
        for (ComboOffer combo : availableCombos) {
            String id1 = combo.getItem1().getId();
            String id2 = combo.getItem2().getId();
            
            int available1 = itemQuantities.getOrDefault(id1, 0) - usedQuantities.getOrDefault(id1, 0);
            int available2 = itemQuantities.getOrDefault(id2, 0) - usedQuantities.getOrDefault(id2, 0);
            
            int timesToApply = Math.min(available1, available2);
            
            if (timesToApply > 0) {
                appliedCombos.add(new AppliedCombo(combo, timesToApply));
                usedQuantities.put(id1, usedQuantities.getOrDefault(id1, 0) + timesToApply);
                usedQuantities.put(id2, usedQuantities.getOrDefault(id2, 0) + timesToApply);
            }
        }
    }
    
    public int getSubtotal() {
        return items.stream().mapToInt(CartItem::getItemTotal).sum();
    }
    
    public int getDiscount() {
        return appliedCombos.stream().mapToInt(AppliedCombo::getTotalSavings).sum();
    }
    
    public int getTotal() {
        return getSubtotal() - getDiscount();
    }
    
    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }
    
    public List<AppliedCombo> getAppliedCombos() {
        return new ArrayList<>(appliedCombos);
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
    
    public void clear() {
        items.clear();
        appliedCombos.clear();
    }
    
    public Order checkout(String customerName, String phone, String address, String notes) {
        return new Order(customerName, phone, address, notes, 
                        items, appliedCombos, getSubtotal(), getDiscount(), getTotal());
    }
}

// =============================== 
// MENU RECOMMENDATION SYSTEM (SAMA)
// =============================== 

class MenuRecommendationSystem {
    private List<MenuItem> fullMenu;
    private Map<String, String> synonyms;
    private List<ComboOffer> allCombos;

    public MenuRecommendationSystem() {
        fullMenu = new ArrayList<>();
        synonyms = new HashMap<>();
        allCombos = new ArrayList<>();
        initializeMenu();
        initializeSynonyms();
        initializeCombos();
    }

    private void initializeMenu() {
        // Korean Food
        fullMenu.add(new MenuItem("ITEM001", "Tteokbokki", 25000, "food", 
            "sweet", "spicy", "korean", "rice cake", "street food"));
        fullMenu.add(new MenuItem("ITEM002", "Korean Fried Chicken", 30000, "food", 
            "sweet", "spicy", "korean", "chicken", "crispy", "fried"));
        fullMenu.add(new MenuItem("ITEM003", "Kimchi Fried Rice", 22000, "food", 
            "spicy", "korean", "rice", "kimchi", "savory"));
        fullMenu.add(new MenuItem("ITEM004", "Bibimbap", 28000, "food", 
            "savory", "korean", "rice", "vegetables", "egg", "healthy"));
        
        // Indonesian Food
        fullMenu.add(new MenuItem("ITEM005", "Beef Rendang", 25000, "food", 
            "spicy", "savory", "meat", "beef", "indonesian", "coconut"));
        fullMenu.add(new MenuItem("ITEM006", "Fried Rice", 15000, "food", 
            "salty", "savory", "rice", "indonesian", "egg"));
        fullMenu.add(new MenuItem("ITEM007", "Nasi Goreng Seafood", 20000, "food", 
            "savory", "spicy", "rice", "seafood", "indonesian"));
        fullMenu.add(new MenuItem("ITEM008", "Ayam Geprek", 18000, "food", 
            "spicy", "chicken", "indonesian", "fried", "crispy"));
        
        // Western Food
        fullMenu.add(new MenuItem("ITEM009", "Aglio Olio Pasta", 25000, "food", 
            "savory", "pasta", "italian", "garlic", "western"));
        fullMenu.add(new MenuItem("ITEM010", "Beef Burger", 28000, "food", 
            "savory", "beef", "burger", "western", "cheese"));
        
        // Drinks - Cold
        fullMenu.add(new MenuItem("ITEM011", "Es Teh Manis", 5000, "drink", 
            "sweet", "ice", "cold", "tea", "indonesian"));
        fullMenu.add(new MenuItem("ITEM012", "Iced Latte", 18000, "drink", 
            "sweet", "ice", "cold", "coffee", "milk"));
        fullMenu.add(new MenuItem("ITEM013", "Iced Chocolate", 15000, "drink", 
            "sweet", "ice", "cold", "chocolate", "milk"));
        fullMenu.add(new MenuItem("ITEM014", "Lemon Tea", 12000, "drink", 
            "sweet", "sour", "ice", "cold", "tea", "lemon", "fresh"));
        fullMenu.add(new MenuItem("ITEM015", "Strawberry Smoothie", 20000, "drink", 
            "sweet", "ice", "cold", "fruit", "strawberry", "healthy"));
        
        // Drinks - Hot
        fullMenu.add(new MenuItem("ITEM016", "Hot Latte", 15000, "drink", 
            "sweet", "hot", "warm", "coffee", "milk"));
        fullMenu.add(new MenuItem("ITEM017", "Hot Chocolate", 13000, "drink", 
            "sweet", "hot", "warm", "chocolate", "milk"));
        fullMenu.add(new MenuItem("ITEM018", "Green Tea", 10000, "drink", 
            "hot", "warm", "tea", "healthy", "japanese"));
        
        // Desserts
        fullMenu.add(new MenuItem("ITEM019", "Chocolate Cake", 25000, "dessert", 
            "sweet", "chocolate", "cake", "rich"));
        fullMenu.add(new MenuItem("ITEM020", "Tiramisu", 30000, "dessert", 
            "sweet", "coffee", "cake", "italian", "creamy"));
        fullMenu.add(new MenuItem("ITEM021", "Strawberry Cheesecake", 28000, "dessert", 
            "sweet", "fruit", "strawberry", "cake", "creamy", "cheese"));
        fullMenu.add(new MenuItem("ITEM022", "Ice Cream Sundae", 22000, "dessert", 
            "sweet", "cold", "ice cream", "chocolate", "vanilla"));
    }

    private void initializeSynonyms() {
        synonyms.put("manis", "sweet");
        synonyms.put("pedas", "spicy");
        synonyms.put("asin", "salty");
        synonyms.put("gurih", "savory");
        synonyms.put("asam", "sour");
        synonyms.put("es", "ice");
        synonyms.put("dingin", "cold");
        synonyms.put("panas", "hot");
        synonyms.put("hangat", "warm");
        synonyms.put("makanan", "food");
        synonyms.put("minuman", "drink");
        synonyms.put("pencuci mulut", "dessert");
        synonyms.put("nasi", "rice");
        synonyms.put("ayam", "chicken");
        synonyms.put("sapi", "beef");
        synonyms.put("korea", "korean");
        synonyms.put("indonesia", "indonesian");
        synonyms.put("jepang", "japanese");
        synonyms.put("italia", "italian");
        synonyms.put("barat", "western");
    }
    
    private void initializeCombos() {
        MenuItem koreanChicken = getMenuItemById("ITEM002");
        MenuItem tteokbokki = getMenuItemById("ITEM001");
        MenuItem rendang = getMenuItemById("ITEM005");
        MenuItem burger = getMenuItemById("ITEM010");
        MenuItem ayamGeprek = getMenuItemById("ITEM008");
        
        MenuItem icedLatte = getMenuItemById("ITEM012");
        MenuItem lemonTea = getMenuItemById("ITEM014");
        MenuItem icedChocolate = getMenuItemById("ITEM013");
        MenuItem esTeh = getMenuItemById("ITEM011");
        
        allCombos.add(new ComboOffer(koreanChicken, icedLatte, 0.20));
        allCombos.add(new ComboOffer(koreanChicken, lemonTea, 0.20));
        allCombos.add(new ComboOffer(tteokbokki, icedChocolate, 0.20));
        allCombos.add(new ComboOffer(rendang, esTeh, 0.20));
        allCombos.add(new ComboOffer(burger, icedLatte, 0.20));
        allCombos.add(new ComboOffer(ayamGeprek, esTeh, 0.20));
        
        MenuItem chocolateCake = getMenuItemById("ITEM019");
        MenuItem tiramisu = getMenuItemById("ITEM020");
        MenuItem cheesecake = getMenuItemById("ITEM021");
        
        allCombos.add(new ComboOffer(koreanChicken, chocolateCake, 0.25));
        allCombos.add(new ComboOffer(burger, tiramisu, 0.25));
        allCombos.add(new ComboOffer(rendang, cheesecake, 0.25));
        
        allCombos.add(new ComboOffer(icedLatte, chocolateCake, 0.15));
        allCombos.add(new ComboOffer(icedLatte, tiramisu, 0.15));
        allCombos.add(new ComboOffer(icedChocolate, cheesecake, 0.15));
    }
    
    private MenuItem getMenuItemById(String id) {
        return fullMenu.stream()
            .filter(item -> item.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public RecommendationResult getRecommendations(String userQuery) {
        Set<String> queryTags = parseQuery(userQuery.toLowerCase());
        String requestedCategory = detectCategory(userQuery.toLowerCase());

        List<MenuItem> matches = new ArrayList<>();
        
        // Exact match first
        for (MenuItem item : fullMenu) {
            if (requestedCategory != null && !item.getCategory().equals(requestedCategory)) {
                continue;
            }
            if (item.hasAllTags(queryTags) && !queryTags.isEmpty()) {
                matches.add(item);
            }
        }

        // Partial match
        if (matches.isEmpty() && !queryTags.isEmpty()) {
            Map<MenuItem, Integer> scoredItems = new HashMap<>();
            for (MenuItem item : fullMenu) {
                if (requestedCategory != null && !item.getCategory().equals(requestedCategory)) {
                    continue;
                }
                int score = item.getMatchScore(queryTags);
                if (score > 0) {
                    scoredItems.put(item, score);
                }
            }
            matches = scoredItems.entrySet().stream()
                .sorted(Map.Entry.<MenuItem, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(8)
                .collect(Collectors.toList());
        }

        if (matches.isEmpty()) {
            matches = fullMenu.stream().limit(8).collect(Collectors.toList());
        }

        List<MenuItem> topItems = matches.stream().limit(8).collect(Collectors.toList());
        List<ComboOffer> suggestedCombos = generateSuggestedCombos(topItems);
        
        return new RecommendationResult(userQuery, topItems, suggestedCombos, queryTags, requestedCategory);
    }

    private String detectCategory(String query) {
        if (query.contains("food") || query.contains("makanan") || 
            query.contains("makan") || query.contains("lapar")) return "food";
        if (query.contains("drink") || query.contains("minuman") || 
            query.contains("minum") || query.contains("haus")) return "drink";
        if (query.contains("dessert") || query.contains("pencuci mulut") || 
            query.contains("manis")) return "dessert";
        return null;
    }

    private Set<String> parseQuery(String query) {
        Set<String> tags = new HashSet<>();
        String[] words = query.split("[\\s,+&]+");
        for (String word : words) {
            word = word.trim().toLowerCase();
            if (synonyms.containsKey(word)) {
                tags.add(synonyms.get(word));
            } else if (isValidTag(word)) {
                tags.add(word);
            }
        }
        return tags;
    }

    private boolean isValidTag(String tag) {
        for (MenuItem item : fullMenu) {
            if (item.getTags().contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private List<ComboOffer> generateSuggestedCombos(List<MenuItem> items) {
        List<ComboOffer> suggested = new ArrayList<>();
        Set<String> itemIds = items.stream()
            .map(MenuItem::getId)
            .collect(Collectors.toSet());
        
        for (ComboOffer combo : allCombos) {
            if (itemIds.contains(combo.getItem1().getId()) && 
                itemIds.contains(combo.getItem2().getId())) {
                suggested.add(combo);
            }
        }
        
        return suggested.stream().limit(3).collect(Collectors.toList());
    }
    
    public List<ComboOffer> getAllCombos() {
        return new ArrayList<>(allCombos);
    }
    
    public List<MenuItem> getFullMenu() {
        return new ArrayList<>(fullMenu);
    }
}

class RecommendationResult {
    private String originalQuery;
    private List<MenuItem> recommendedItems;
    private List<ComboOffer> suggestedCombos;
    private Set<String> detectedTags;
    private String requestedCategory;

    public RecommendationResult(String query, List<MenuItem> items, List<ComboOffer> combos, 
                                Set<String> tags, String category) {
        this.originalQuery = query;
        this.recommendedItems = items;
        this.suggestedCombos = combos;
        this.detectedTags = tags;
        this.requestedCategory = category;
    }

    public String getOriginalQuery() { return originalQuery; }
    public List<MenuItem> getRecommendedItems() { return recommendedItems; }
    public List<ComboOffer> getSuggestedCombos() { return suggestedCombos; }
    public Set<String> getDetectedTags() { return detectedTags; }
    public String getRequestedCategory() { return requestedCategory; }
}

// =============================== 
// BUYER WINDOW (DIRESTRUKTURISASI)
// =============================== 

class BuyerChatWindow extends JFrame {
    private JPanel chatContainer;
    private JPanel cartPanel;
    private JTextField inputField;
    private JLabel cartCountLabel;
    private JLabel cartTotalLabel;
    private MenuRecommendationSystem recommendationSystem;
    private ShoppingCart shoppingCart;
    private SellerWindow sellerWindow;
    private List<ChatBlock> chatHistory;

    public BuyerChatWindow(SellerWindow sellerWindow) {
        this.sellerWindow = sellerWindow;
        this.recommendationSystem = new MenuRecommendationSystem();
        this.shoppingCart = new ShoppingCart(recommendationSystem);
        this.chatHistory = new ArrayList<>();
        
        setTitle("Buyer - Smart Menu with Shopping Cart");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(900, 50);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);
        splitPane.setResizeWeight(0.7);
        
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(new Color(252, 252, 252));
        JScrollPane menuScroll = new JScrollPane(chatContainer);
        menuScroll.setBorder(null);
        splitPane.setLeftComponent(menuScroll);
        
        cartPanel = createCartPanel();
        splitPane.setRightComponent(cartPanel);
        
        add(splitPane, BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        addWelcomeBlocks();
        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(60, 60, 80));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel title = new JLabel("🛒 Smart Menu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        cartCountLabel = new JLabel("Cart: 0 items");
        cartCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cartCountLabel.setForeground(new Color(200, 200, 200));
        
        cartTotalLabel = new JLabel("Total: Rp 0");
        cartTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cartTotalLabel.setForeground(new Color(100, 250, 100));
        
        rightPanel.add(cartCountLabel);
        rightPanel.add(cartTotalLabel);
        
        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(220, 220, 220)));
        
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setBackground(new Color(245, 245, 250));
        cartHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel cartTitle = new JLabel("🛒 Shopping Cart");
        cartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JButton clearBtn = new JButton("Clear All");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setForeground(new Color(200, 100, 100));
        clearBtn.setBorderPainted(false);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clearCart());
        
        cartHeader.add(cartTitle, BorderLayout.WEST);
        cartHeader.add(clearBtn, BorderLayout.EAST);
        
        panel.add(cartHeader, BorderLayout.NORTH);
        
        return panel;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        bar.setBackground(Color.WHITE);

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        inputField.addActionListener(e -> processUserRequest());

        JButton searchBtn = new JButton("🔍 Search Menu");
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchBtn.setBackground(new Color(100, 150, 250));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 130, 230), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        searchBtn.setFocusPainted(false);
        searchBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchBtn.addActionListener(e -> processUserRequest());

        bar.add(inputField, BorderLayout.CENTER);
        bar.add(searchBtn, BorderLayout.EAST);

        return bar;
    }

    private void addWelcomeBlocks() {
        // MessageBlock: Welcome message
        MessageBlock welcomeMsg = new MessageBlock(
            "Welcome to Smart Menu! Search by taste, cuisine, or category:", false);
        chatHistory.add(welcomeMsg);
        chatContainer.add(welcomeMsg.createComponent());
        
        // MnRBlock: Welcome with examples
        Set<String> exampleTags = new HashSet<>(Arrays.asList("sweet", "spicy", "korean", "indonesian"));
        List<MenuItem> exampleItems = recommendationSystem.getFullMenu().subList(0, 4);
        
        MnRBlock exampleBlock = new MnRBlock(
            "Example queries:", 
            exampleItems,
            new ArrayList<>(),
            exampleTags,
            null,
            this
        );
        chatHistory.add(exampleBlock);
        chatContainer.add(exampleBlock.createComponent());
    }

    private void processUserRequest() {
        String query = inputField.getText().trim();
        if (!query.isEmpty()) {
            // 1. Add MessageBlock untuk user query
            MessageBlock userMessage = new MessageBlock(query, true);
            chatHistory.add(userMessage);
            chatContainer.add(userMessage.createComponent());
            
            javax.swing.Timer timer = new javax.swing.Timer(300, e -> {
                RecommendationResult result = recommendationSystem.getRecommendations(query);
                
                // 2. Add MnRBlock untuk hasil rekomendasi
                MnRBlock resultBlock = new MnRBlock(
                    "Menu Results for: \"" + query + "\"",
                    result.getRecommendedItems(),
                    result.getSuggestedCombos(),
                    result.getDetectedTags(),
                    result.getRequestedCategory(),
                    this
                );
                chatHistory.add(resultBlock);
                chatContainer.add(resultBlock.createComponent());
                
                chatContainer.revalidate();
                chatContainer.repaint();
                
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = ((JScrollPane)chatContainer.getParent().getParent()).getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            });
            timer.setRepeats(false);
            timer.start();
            inputField.setText("");
        }
    }
    
    public void addToCart(MenuItem item, int quantity) {
        shoppingCart.addItem(item, quantity);
        updateCartDisplay();
        
        // Add MessageBlock untuk feedback
        MessageBlock feedback = new MessageBlock(
            String.format("Added %dx %s to cart!", quantity, item.getName()), false);
        chatHistory.add(feedback);
        chatContainer.add(feedback.createComponent());
        
        chatContainer.revalidate();
        chatContainer.repaint();
    }
    
    public void addComboToCart(ComboOffer combo) {
        shoppingCart.addItem(combo.getItem1(), 1);
        shoppingCart.addItem(combo.getItem2(), 1);
        updateCartDisplay();
        
        MessageBlock feedback = new MessageBlock(
            String.format("Added combo: %s + %s\nSave Rp %,d!", 
                combo.getItem1().getName(), combo.getItem2().getName(), combo.getSavings()), 
            false);
        chatHistory.add(feedback);
        chatContainer.add(feedback.createComponent());
        
        chatContainer.revalidate();
        chatContainer.repaint();
    }
    
    private void updateCartDisplay() {
        cartCountLabel.setText("Cart: " + shoppingCart.getItemCount() + " items");
        cartTotalLabel.setText("Total: Rp " + String.format("%,d", shoppingCart.getTotal()));
        
        cartPanel.removeAll();
        
        JPanel cartHeader = new JPanel(new BorderLayout());
        cartHeader.setBackground(new Color(245, 245, 250));
        cartHeader.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel cartTitle = new JLabel("🛒 Shopping Cart");
        cartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JButton clearBtn = new JButton("Clear All");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setForeground(new Color(200, 100, 100));
        clearBtn.setBorderPainted(false);
        clearBtn.setContentAreaFilled(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> clearCart());
        
        cartHeader.add(cartTitle, BorderLayout.WEST);
        cartHeader.add(clearBtn, BorderLayout.EAST);
        
        cartPanel.add(cartHeader, BorderLayout.NORTH);
        
        if (shoppingCart.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Color.WHITE);
            JLabel emptyLabel = new JLabel("<html><center>Cart is empty<br>Start adding items!</center></html>");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            emptyLabel.setForeground(Color.GRAY);
            emptyPanel.add(emptyLabel);
            cartPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            for (CartItem item : shoppingCart.getItems()) {
                contentPanel.add(createCartItemRow(item));
                contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
            
            if (!shoppingCart.getAppliedCombos().isEmpty()) {
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                contentPanel.add(createSeparator());
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                
                JLabel comboLabel = new JLabel("💡 Applied Combos:");
                comboLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                comboLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(comboLabel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                
                for (AppliedCombo appliedCombo : shoppingCart.getAppliedCombos()) {
                    contentPanel.add(createAppliedComboRow(appliedCombo));
                    contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }
            
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            contentPanel.add(createSeparator());
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            
            contentPanel.add(createSummaryRow("Subtotal:", shoppingCart.getSubtotal(), false));
            if (shoppingCart.getDiscount() > 0) {
                contentPanel.add(createSummaryRow("Discount:", -shoppingCart.getDiscount(), true));
            }
            contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            contentPanel.add(createSeparator());
            contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            contentPanel.add(createSummaryRow("TOTAL:", shoppingCart.getTotal(), false));
            
            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            cartPanel.add(scrollPane, BorderLayout.CENTER);
            
            JButton checkoutBtn = new JButton("💳 Checkout");
            checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            checkoutBtn.setBackground(new Color(100, 200, 100));
            checkoutBtn.setForeground(Color.WHITE);
            checkoutBtn.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            checkoutBtn.setFocusPainted(false);
            checkoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            checkoutBtn.addActionListener(e -> showCheckoutDialog());
            
            JPanel checkoutPanel = new JPanel(new BorderLayout());
            checkoutPanel.setBackground(Color.WHITE);
            checkoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            checkoutPanel.add(checkoutBtn, BorderLayout.CENTER);
            
            cartPanel.add(checkoutPanel, BorderLayout.SOUTH);
        }
        
        cartPanel.revalidate();
        cartPanel.repaint();
    }
    
    private JPanel createCartItemRow(CartItem item) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(new Color(250, 250, 250));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(item.getMenuItem().getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel priceLabel = new JLabel(String.format("Rp %,d × %d", 
            item.getMenuItem().getPrice(), item.getQuantity()));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        priceLabel.setForeground(Color.GRAY);
        
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        qtyPanel.setOpaque(false);
        
        JButton minusBtn = new JButton("−");
        minusBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        minusBtn.setPreferredSize(new Dimension(30, 25));
        minusBtn.setFocusPainted(false);
        minusBtn.addActionListener(e -> {
            shoppingCart.updateQuantity(item.getMenuItem().getId(), item.getQuantity() - 1);
            updateCartDisplay();
        });
        
        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()));
        qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        qtyLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        
        JButton plusBtn = new JButton("+");
        plusBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        plusBtn.setPreferredSize(new Dimension(30, 25));
        plusBtn.setFocusPainted(false);
        plusBtn.addActionListener(e -> {
            shoppingCart.updateQuantity(item.getMenuItem().getId(), item.getQuantity() + 1);
            updateCartDisplay();
        });
        
        JButton removeBtn = new JButton("🗑");
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        removeBtn.setPreferredSize(new Dimension(30, 25));
        removeBtn.setForeground(new Color(200, 100, 100));
        removeBtn.setFocusPainted(false);
        removeBtn.addActionListener(e -> {
            shoppingCart.removeItem(item.getMenuItem().getId());
            updateCartDisplay();
        });
        
        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plusBtn);
        qtyPanel.add(removeBtn);
        
        JLabel totalLabel = new JLabel(String.format("Rp %,d", item.getItemTotal()));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(totalLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        rightPanel.add(qtyPanel);
        
        row.add(infoPanel, BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createAppliedComboRow(AppliedCombo appliedCombo) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(240, 255, 240));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 200, 100), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        ComboOffer combo = appliedCombo.getCombo();
        
        JLabel comboLabel = new JLabel(String.format("✓ %s + %s ×%d", 
            combo.getItem1().getName(), 
            combo.getItem2().getName(),
            appliedCombo.getTimesApplied()));
        comboLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JLabel savingsLabel = new JLabel(String.format("−Rp %,d", appliedCombo.getTotalSavings()));
        savingsLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        savingsLabel.setForeground(new Color(0, 150, 0));
        
        row.add(comboLabel, BorderLayout.CENTER);
        row.add(savingsLabel, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createSummaryRow(String label, int amount, boolean isDiscount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", label.contains("TOTAL") ? Font.BOLD : Font.PLAIN, 
                                    label.contains("TOTAL") ? 14 : 12));
        
        JLabel amountText = new JLabel(String.format("%sRp %,d", 
            amount < 0 ? "−" : "", Math.abs(amount)));
        amountText.setFont(new Font("Segoe UI", label.contains("TOTAL") ? Font.BOLD : Font.PLAIN, 
                                     label.contains("TOTAL") ? 14 : 12));
        if (isDiscount) {
            amountText.setForeground(new Color(0, 150, 0));
        } else if (label.contains("TOTAL")) {
            amountText.setForeground(new Color(50, 100, 200));
        }
        
        row.add(labelText, BorderLayout.WEST);
        row.add(amountText, BorderLayout.EAST);
        
        return row;
    }
    
    private JPanel createSeparator() {
        JPanel separator = new JPanel();
        separator.setBackground(new Color(230, 230, 230));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        return separator;
    }
    
    private void clearCart() {
        if (shoppingCart.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear the cart?",
            "Clear Cart",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            shoppingCart.clear();
            updateCartDisplay();
            
            // Add MessageBlock for feedback
            MessageBlock feedback = new MessageBlock("Cart cleared!", false);
            chatHistory.add(feedback);
            chatContainer.add(feedback.createComponent());
            chatContainer.revalidate();
            chatContainer.repaint();
        }
    }
    
    private void showCheckoutDialog() {
        JDialog dialog = new JDialog(this, "Checkout", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.weightx = 1.0;
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Customer Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField(30);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setPreferredSize(new Dimension(350, 35));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel phoneLabel = new JLabel("Phone Number:");
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField phoneField = new JTextField(30);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phoneField.setPreferredSize(new Dimension(350, 35));
        phoneField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        formPanel.add(phoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel addressLabel = new JLabel("Delivery Address:");
        addressLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(addressLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        JTextArea addressArea = new JTextArea(4, 30);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setPreferredSize(new Dimension(350, 90));
        formPanel.add(addressScroll, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel notesLabel = new JLabel("Special Notes:");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formPanel.add(notesLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        JTextArea notesArea = new JTextArea(3, 30);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setPreferredSize(new Dimension(350, 70));
        formPanel.add(notesScroll, gbc);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(new Color(245, 245, 250));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel summaryLabel = new JLabel(String.format(
            "<html><b>Order Summary:</b><br>" +
            "Items: %d<br>" +
            "Subtotal: Rp %,d<br>" +
            "Discount: Rp %,d<br>" +
            "<font color='blue'><b>Total: Rp %,d</b></font></html>",
            shoppingCart.getItemCount(),
            shoppingCart.getSubtotal(),
            shoppingCart.getDiscount(),
            shoppingCart.getTotal()
        ));
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summaryPanel.add(summaryLabel);
        
        dialog.add(summaryPanel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton placeOrderBtn = new JButton("Place Order");
        placeOrderBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        placeOrderBtn.setBackground(new Color(100, 200, 100));
        placeOrderBtn.setForeground(Color.WHITE);
        placeOrderBtn.setFocusPainted(false);
        placeOrderBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();
            String notes = notesArea.getText().trim();
            
            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill in all required fields!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Order order = shoppingCart.checkout(name, phone, address, notes);
            sellerWindow.receiveOrder(order);
            
            dialog.dispose();
            
            MessageBlock orderPlaced = new MessageBlock(
                String.format("Order placed successfully! Order ID: %s", order.getOrderId()), false);
            chatHistory.add(orderPlaced);
            chatContainer.add(orderPlaced.createComponent());
            
            chatContainer.revalidate();
            chatContainer.repaint();
            
            shoppingCart.clear();
            updateCartDisplay();
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(placeOrderBtn);
        
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}

// =============================== 
// SELLER WINDOW (TETAP SAMA)
// =============================== 

class SellerWindow extends JFrame {
    private JPanel orderListPanel;
    private Queue<Order> pendingOrders;
    private List<Order> allOrders;
    private JLabel statsLabel;

    public SellerWindow() {
        pendingOrders = new LinkedList<>();
        allOrders = new ArrayList<>();
        
        setTitle("Seller Dashboard - Order Management");
        setSize(900, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(50, 50);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        orderListPanel = new JPanel();
        orderListPanel.setLayout(new BoxLayout(orderListPanel, BoxLayout.Y_AXIS));
        orderListPanel.setBackground(new Color(245, 245, 250));
        orderListPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(orderListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        addWelcomeMessage();

        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(60, 60, 80));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("📊 Seller Order Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        statsLabel = new JLabel("Orders: 0 total | 0 pending");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsLabel.setForeground(new Color(200, 200, 200));
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(statsLabel, BorderLayout.EAST);
        
        return header;
    }
    
    private void addWelcomeMessage() {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setBackground(new Color(245, 245, 250));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JLabel welcomeLabel = new JLabel("<html><center>" +
            "<h2>Seller Dashboard</h2>" +
            "<p>Waiting for orders from customers...</p>" +
            "</center></html>");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(Color.GRAY);
        
        welcomePanel.add(welcomeLabel);
        orderListPanel.add(welcomePanel);
    }

    public void receiveOrder(Order order) {
        if (orderListPanel.getComponentCount() > 0 && 
            orderListPanel.getComponent(0) instanceof JPanel) {
            Component firstComp = orderListPanel.getComponent(0);
            if (firstComp instanceof JPanel) {
                JPanel panel = (JPanel) firstComp;
                if (panel.getComponentCount() > 0 && 
                    panel.getComponent(0) instanceof JLabel) {
                    orderListPanel.removeAll();
                }
            }
        }
        
        pendingOrders.add(order);
        allOrders.add(order);
        
        OrderCard card = new OrderCard(order, this);
        orderListPanel.add(card, 0);
        orderListPanel.add(Box.createRigidArea(new Dimension(0, 15)), 1);
        
        orderListPanel.revalidate();
        orderListPanel.repaint();
        
        updateStats();
        
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = ((JScrollPane)orderListPanel.getParent().getParent())
                .getVerticalScrollBar();
            vertical.setValue(0);
        });
    }
    
    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        order.setStatus(newStatus);
        if (newStatus != OrderStatus.PENDING) {
            pendingOrders.remove(order);
        }
        updateStats();
    }
    
    private void updateStats() {
        int total = allOrders.size();
        int pending = pendingOrders.size();
        statsLabel.setText(String.format("Orders: %d total | %d pending", total, pending));
    }
}

// OrderCard tetap sama seperti sebelumnya...
// [OrderCard class tetap sama seperti kode awal Anda]
// =============================== 
// ORDER CARD (Seller View)
// =============================== 

class OrderCard extends JPanel {
    private Order order;
    private SellerWindow sellerWindow;
    private JPanel statusIndicator;
    private JLabel statusLabel;

    public OrderCard(Order order, SellerWindow sellerWindow) {
        this.order = order;
        this.sellerWindow = sellerWindow;
        
        setLayout(new BorderLayout(15, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(order.getStatus().getColor(), 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        
        // Left: Order details
        add(createOrderDetailsPanel(), BorderLayout.CENTER);
        
        // Right: Actions
        add(createActionPanel(), BorderLayout.EAST);
    }
    
    private JPanel createOrderDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        // Header: Order ID + Status
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel orderIdLabel = new JLabel("Order #" + order.getOrderId());
        orderIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        statusLabel = new JLabel(order.getStatus().getDisplayName());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(order.getStatus().getColor());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        
        headerPanel.add(orderIdLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        panel.add(headerPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Time
        JLabel timeLabel = new JLabel("⏰ " + order.getFormattedTime());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(timeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Customer info
        JPanel customerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        customerPanel.setOpaque(false);
        customerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel nameLabel = new JLabel("👤 " + order.getCustomerName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel phoneLabel = new JLabel("📞 " + order.getPhoneNumber());
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel addressLabel = new JLabel("📍 " + order.getDeliveryAddress());
        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        customerPanel.add(nameLabel);
        customerPanel.add(phoneLabel);
        customerPanel.add(addressLabel);
        
        panel.add(customerPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Items list
        JLabel itemsHeader = new JLabel("📦 Order Items:");
        itemsHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(itemsHeader);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        for (CartItem item : order.getItems()) {
            JLabel itemLabel = new JLabel(String.format("  • %s ×%d - Rp %,d",
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getItemTotal()));
            itemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(itemLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 3)));
        }
        
        // Combos
        if (!order.getAppliedCombos().isEmpty()) {
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            JLabel comboHeader = new JLabel("💡 Applied Combos:");
            comboHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
            comboHeader.setForeground(new Color(0, 150, 0));
            comboHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(comboHeader);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
            
            for (AppliedCombo combo : order.getAppliedCombos()) {
                JLabel comboLabel = new JLabel(String.format("  ✓ %s + %s ×%d (−Rp %,d)",
                    combo.getCombo().getItem1().getName(),
                    combo.getCombo().getItem2().getName(),
                    combo.getTimesApplied(),
                    combo.getTotalSavings()));
                comboLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                comboLabel.setForeground(new Color(0, 130, 0));
                comboLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(comboLabel);
                panel.add(Box.createRigidArea(new Dimension(0, 3)));
            }
        }
        
        // Special notes
        if (order.getSpecialNotes() != null && !order.getSpecialNotes().isEmpty()) {
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            JLabel notesHeader = new JLabel("📝 Special Notes:");
            notesHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
            notesHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(notesHeader);
            
            JTextArea notesArea = new JTextArea(order.getSpecialNotes());
            notesArea.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            notesArea.setForeground(Color.GRAY);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            notesArea.setEditable(false);
            notesArea.setOpaque(false);
            notesArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(notesArea);
        }
        
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Total
        JPanel totalPanel = new JPanel(new GridLayout(3, 2, 10, 3));
        totalPanel.setOpaque(false);
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        totalPanel.add(new JLabel("Subtotal:"));
        JLabel subtotalValue = new JLabel(String.format("Rp %,d", order.getSubtotal()));
        subtotalValue.setHorizontalAlignment(SwingConstants.RIGHT);
        totalPanel.add(subtotalValue);
        
        totalPanel.add(new JLabel("Discount:"));
        JLabel discountValue = new JLabel(String.format("−Rp %,d", order.getDiscount()));
        discountValue.setHorizontalAlignment(SwingConstants.RIGHT);
        discountValue.setForeground(new Color(0, 150, 0));
        totalPanel.add(discountValue);
        
        JLabel totalLabel = new JLabel("TOTAL:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalPanel.add(totalLabel);
        
        JLabel totalValue = new JLabel(String.format("Rp %,d", order.getTotal()));
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalValue.setForeground(new Color(50, 100, 200));
        totalValue.setHorizontalAlignment(SwingConstants.RIGHT);
        totalPanel.add(totalValue);
        
        panel.add(totalPanel);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        if (order.getStatus() == OrderStatus.PENDING) {
            JButton confirmBtn = createActionButton("✅ Confirm", new Color(100, 200, 100));
            confirmBtn.addActionListener(e -> updateStatus(OrderStatus.CONFIRMED));
            panel.add(confirmBtn);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
            
            JButton rejectBtn = createActionButton("❌ Reject", new Color(250, 100, 100));
            rejectBtn.addActionListener(e -> rejectOrder());
            panel.add(rejectBtn);
        } else if (order.getStatus() == OrderStatus.CONFIRMED) {
            JButton prepareBtn = createActionButton("👨‍🍳 Start Preparing", new Color(150, 100, 250));
            prepareBtn.addActionListener(e -> updateStatus(OrderStatus.PREPARING));
            panel.add(prepareBtn);
        } else if (order.getStatus() == OrderStatus.PREPARING) {
            JButton readyBtn = createActionButton("✓ Mark Ready", new Color(100, 200, 150));
            readyBtn.addActionListener(e -> updateStatus(OrderStatus.READY));
            panel.add(readyBtn);
        } else if (order.getStatus() == OrderStatus.READY) {
            JButton completeBtn = createActionButton("🎉 Complete", new Color(100, 250, 100));
            completeBtn.addActionListener(e -> updateStatus(OrderStatus.COMPLETED));
            panel.add(completeBtn);
        }
        
        return panel;
    }
    
    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private void updateStatus(OrderStatus newStatus) {
        sellerWindow.updateOrderStatus(order, newStatus);
        
        // Update UI
        statusLabel.setText(newStatus.getDisplayName());
        statusLabel.setBackground(newStatus.getColor());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(newStatus.getColor(), 3),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Rebuild action panel
        remove(getComponent(1)); // Remove old action panel
        add(createActionPanel(), BorderLayout.EAST);
        revalidate();
        repaint();
        
        String message = "";
        switch (newStatus) {
            case CONFIRMED:
                message = "Order confirmed! Ready to prepare.";
                break;
            case PREPARING:
                message = "Started preparing the order.";
                break;
            case READY:
                message = "Order is ready for pickup/delivery!";
                break;
            case COMPLETED:
                message = "Order completed successfully!";
                break;
        }
        
        JOptionPane.showMessageDialog(this, message, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void rejectOrder() {
        String reason = JOptionPane.showInputDialog(this,
            "Reason for rejection:",
            "Reject Order",
            JOptionPane.QUESTION_MESSAGE);
        
        if (reason != null && !reason.trim().isEmpty()) {
            sellerWindow.updateOrderStatus(order, OrderStatus.REJECTED);
            
            statusLabel.setText(OrderStatus.REJECTED.getDisplayName());
            statusLabel.setBackground(OrderStatus.REJECTED.getColor());
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OrderStatus.REJECTED.getColor(), 3),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            
            // Remove action panel
            remove(getComponent(1));
            
            // Add rejection reason
            JLabel reasonLabel = new JLabel("<html><i>Rejected: " + reason + "</i></html>");
            reasonLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            reasonLabel.setForeground(new Color(200, 100, 100));
            add(reasonLabel, BorderLayout.SOUTH);
            
            revalidate();
            repaint();
            
            JOptionPane.showMessageDialog(this, 
                "Order rejected: " + reason, 
                "Order Rejected", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
