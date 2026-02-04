# oovp-exercise-1add
# 🛒 Smart Menu E-Commerce Chat Application

## 📋 Overview
**Smart Menu** adalah aplikasi e-commerce yang menggabungkan **sistem chat** dengan **shopping cart** dan **dashboard seller** dalam satu platform terintegrasi. Aplikasi ini menggunakan pola **MessageBlock, RowBlock, dan MnR** untuk menampilkan konten secara terstruktur.

## 🎯 Features

### 🛍️ **Buyer Features**
- **Smart Menu Search** - Pencarian menu berdasarkan rasa, kategori, atau masakan
  - Support multiple languages (English & Indonesian)
  - Tag-based recommendation system
- **Shopping Cart** dengan auto-combo detection
  - Deteksi otomatis combo deals
  - Real-time price calculation
- **Checkout System** lengkap
  - Form customer details
  - Order summary
- **Chat Interface** dengan pola MessageBlock/RowBlock/MnR

### 📊 **Seller Features**
- **Real-time Order Dashboard**
- **Order Status Management** (Pending → Confirmed → Preparing → Ready → Completed)
- **Order Rejection** dengan reason
- **Order Statistics**

## 🏗️ Architecture & Design Patterns

### **Chat Block Pattern**
```java
interface ChatBlock {
    JPanel createComponent();
    String getType();
}

// 1. MessageBlock - Hanya menampilkan pesan
class MessageBlock implements ChatBlock

// 2. RowBlock - Hanya menampilkan rows/grid items  
class RowBlock implements ChatBlock

// 3. MnRBlock - Menampilkan pesan DAN rows (Message + Rows)
class MnRBlock implements ChatBlock
```

### **Class Structure**
```
📦 Smart Menu App
├── 📁 Chat Blocks (Design Pattern)
│   ├── ChatBlock (Interface)
│   ├── MessageBlock (Hanya message)
│   ├── RowBlock (Hanya rows)
│   ├── ComboRowBlock (Specialized RowBlock)
│   └── MnRBlock (Message + Rows)
│
├── 📁 Data Models
│   ├── MenuItem (Menu data)
│   ├── CartItem (Cart item)
│   ├── ComboOffer (Combo deals)
│   ├── Order (Order data)
│   └── OrderStatus (Status enum)
│
├── 📁 Systems
│   ├── ShoppingCart (Cart management)
│   └── MenuRecommendationSystem (Search & recommendation)
│
├── 📁 UI Windows
│   ├── BuyerChatWindow (Chat + Cart interface)
│   ├── SellerWindow (Order dashboard)
│   └── OrderCard (Order display component)
│
└── IntegratedChatApp (Main entry point)
```

## 🚀 Getting Started

### **Prerequisites**
- Java JDK 8 or higher
- IDE (IntelliJ, Eclipse, VS Code)

### **Running the Application**
```java
// Run the main class
public class IntegratedChatApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            sellerWindow = new SellerWindow();
            new BuyerChatWindow(sellerWindow);
        });
    }
}
```

### **Application Windows**
1. **Buyer Window** (1100x800) - Untuk customer
2. **Seller Window** (900x800) - Untuk penjual

## 💡 How It Works

### **1. Menu Search & Recommendation**
```
User: "spicy korean food"
↓
System: Parse query → "spicy", "korean", "food"
↓
Recommendation: Filter items matching tags
↓
Display: MnRBlock with results
```

### **2. Shopping Cart with Auto-Combo**
```
Add: Korean Chicken + Iced Latte
↓
System: Detect combo offer (20% discount)
↓
Cart: Apply discount automatically
↓
Update: Real-time price calculation
```

### **3. Order Flow**
```
Buyer: Checkout → Fill details → Place order
↓
Seller: Receive order → Update status
↓
Status: Pending → Confirmed → Preparing → Ready → Completed
```

## 🔧 Technical Implementation

### **Menu Recommendation System**
- **Tag-based matching** dengan synonym support
- **Category detection** (food/drink/dessert)
- **Partial matching** dengan scoring system
- **Combo suggestion** berdasarkan items

### **Shopping Cart Logic**
- **Auto combo detection** (greedy algorithm)
- **Real-time price updates**
- **Quantity management**
- **Persistent cart state**

### **UI Components**
- **Swing-based** dengan modern styling
- **Responsive layouts** (JSplitPane, GridLayout, BoxLayout)
- **Custom components** (OrderCard, Item cards)
- **Real-time updates** tanpa refresh

## 📊 Data Models

### **MenuItem**
```java
class MenuItem {
    String id, name, category;
    int price;
    Set<String> tags; // sweet, spicy, korean, etc.
}
```

### **Order Status Flow**
```
PENDING → CONFIRMED → PREPARING → READY → COMPLETED
      ↓
    REJECTED (with reason)
```

## 🎨 UI Features

### **Buyer Interface**
- **Chat-style layout** dengan message bubbles
- **Shopping cart sidebar** dengan real-time updates
- **Item cards** dengan add to cart functionality
- **Combo deal highlights**

### **Seller Dashboard**
- **Order cards** dengan color-coded status
- **Action buttons** berdasarkan status
- **Order statistics** (total/pending)
- **Rejection with reason input**

## 🔍 Search Examples

### **Supported Queries**
- **By taste**: "sweet", "spicy", "salty", "savory"
- **By cuisine**: "korean", "indonesian", "italian", "western"
- **By category**: "food", "drink", "dessert"
- **Combined**: "spicy korean food", "sweet cold drink"

### **Language Support**
- **English**: "spicy", "sweet", "food", "drink"
- **Indonesian**: "pedas", "manis", "makanan", "minuman"

## 💰 Combo Deals

### **Available Combos**
1. **Food + Drink** (20% discount)
   - Korean Chicken + Iced Latte
   - Beef Rendang + Es Teh
   
2. **Food + Dessert** (25% discount)
   - Korean Chicken + Chocolate Cake
   
3. **Drink + Dessert** (15% discount)
   - Iced Latte + Tiramisu

### **Auto Detection**
System automatically detects and applies best combo deals based on cart items.

## 🛠️ Development Notes

### **Design Patterns Used**
1. **Chat Block Pattern** (MessageBlock, RowBlock, MnR)
2. **Model-View-Controller** (Data models + UI components)
3. **Observer Pattern** (Real-time updates)
4. **Strategy Pattern** (Recommendation algorithms)

### **Key Classes**
- **`BuyerChatWindow`**: Main buyer interface dengan chat + cart
- **`SellerWindow`**: Order management dashboard
- **`ShoppingCart`**: Business logic untuk cart operations
- **`MenuRecommendationSystem`**: Search & recommendation engine

## 📝 Code Examples

### **Creating a MessageBlock**
```java
MessageBlock welcome = new MessageBlock("Welcome to Smart Menu!", false);
chatContainer.add(welcome.createComponent());
```

### **Creating a RowBlock**
```java
RowBlock itemsGrid = new RowBlock(recommendedItems, buyerWindow);
chatContainer.add(itemsGrid.createComponent());
```

### **Creating an MnRBlock**
```java
MnRBlock results = new MnRBlock(
    "Search Results", 
    items, 
    combos, 
    tags, 
    category, 
    buyerWindow
);
chatContainer.add(results.createComponent());
```

## 🔄 Real-time Features

### **Live Updates**
- Cart total updates in real-time
- Order status sync between buyer/seller
- Chat history preservation
- Combo detection on cart changes

### **Event Handling**
- **Add/remove items** → Update cart + detect combos
- **Quantity changes** → Recalculate totals
- **Status updates** → Color changes + action buttons
- **Order placement** → Seller notification

## 🎯 Use Cases

### **For Customers**
1. Search for menu items by preferences
2. Add items to cart with quantity selection
3. Get automatic combo discounts
4. Checkout with delivery details
5. View order status

### **For Sellers**
1. View incoming orders in real-time
2. Update order status through workflow
3. Reject orders with reasons
4. Track order statistics

## 📱 Screenshots (Imaginary)

```
+-----------------------------------------------+
|        SMART MENU - BUYER INTERFACE          |
+----------------------+-----------------------+
|  💬 Chat History     |  🛒 Shopping Cart     |
|  - User: spicy food  |  - Korean Chicken    |
|  - Bot: Results...   |  - Iced Latte        |
|  - [Item Cards]      |  - Combo: -Rp 6,000  |
|  - [Combo Deals]     |  Total: Rp 54,000    |
|                      |  [Checkout Button]    |
+----------------------+-----------------------+
|  [Search Bar: ___________________] [Search]  |
+-----------------------------------------------+
```

```
+-----------------------------------------------+
|       SELLER DASHBOARD - ORDER MGMT          |
+-----------------------------------------------+
| 📊 Orders: 5 total | 2 pending               |
+-----------------------------------------------+
| 🟡 Order #2024-001 - PENDING                |
| 👤 John Doe | 📞 08123456789                |
| 📦 Items: Korean Chicken ×2, Iced Latte ×2  |
| 💰 Total: Rp 108,000                        |
| [✅ Confirm] [❌ Reject]                    |
+-----------------------------------------------+
| 🔵 Order #2024-002 - CONFIRMED              |
| 👤 Jane Smith | Preparing...                |
| [👨‍🍳 Start Preparing]                       |
+-----------------------------------------------+
```

## 🚀 Future Enhancements

### **Planned Features**
1. **User authentication** with login system
2. **Payment gateway integration**
3. **Order history** for customers
4. **Inventory management** for sellers
5. **Rating & review system**
6. **Delivery tracking**
7. **Multi-language support** expansion
8. **Mobile responsive version**

### **Technical Improvements**
1. **Database integration** (MySQL/PostgreSQL)
2. **REST API** for web/mobile clients
3. **WebSocket** for real-time updates
4. **Microservices architecture**
5. **Docker containerization**

## 📚 Learning Points

### **Java Swing Concepts**
- Custom component creation
- Layout management (BorderLayout, GridLayout, BoxLayout)
- Event handling and listeners
- Real-time UI updates

### **Software Design**
- Design pattern implementation
- Separation of concerns
- Interface-based programming
- State management

### **E-commerce Logic**
- Shopping cart algorithms
- Discount calculation
- Order workflow management
- Recommendation systems

## 🤝 Contributing

### **Project Structure**
```
smart-menu-app/
├── src/
│   ├── main/java/
│   │   ├── blocks/       # Chat block pattern classes
│   │   ├── models/       # Data models
│   │   ├── systems/      # Business logic systems
│   │   ├── ui/          # UI windows & components
│   │   └── Main.java    # Entry point
│   └── test/java/       # Unit tests
├── resources/           # Images, icons, etc.
└── README.md           # This file
```

### **Setup for Development**
1. Clone the repository
2. Import as Java project in your IDE
3. Run `IntegratedChatApp.main()`
4. Start development!
