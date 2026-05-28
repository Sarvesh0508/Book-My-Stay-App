package com.bookmystay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {
    private DatabaseHelper dbHelper;
    
    // UI Theme Colors
    private static final Color BG_DARK = new Color(26, 26, 26);
    private static final Color CARD_BG = new Color(44, 44, 44);
    private static final Color GOLD = new Color(212, 175, 55);
    private static final Color TEXT_LIGHT = Color.WHITE;
    private static final Color TEXT_MUTED = new Color(180, 180, 180);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);

    // Cards / Content Layout
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // Navigation Buttons
    private JButton btnDash, btnBook, btnManage, btnAnalytics;
    private JButton activeBtn;

    // Form fields
    private JTextField txtName, txtEmail, txtPhone, txtCheckIn, txtCheckOut;
    private JComboBox<String> comboRooms;
    private JLabel lblCalculatedPrice;
    private List<Map<String, Object>> availableRoomsList;

    // Tables & Statistics
    private JTable resTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalBookingsVal, lblActiveStaysVal, lblRevenueVal, lblOccupancyVal;
    
    // Analytics Graph
    private ChartPanel chartPanel;

    public MainWindow() {
        dbHelper = DatabaseHelper.getInstance();
        
        setTitle("BookMyStay - Luxury Room Booking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        
        initComponents();
        refreshAllData();
    }

    private void initComponents() {
        // Main container panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_DARK);
        setContentPane(mainPanel);

        // --- 1. LEFT SIDEBAR PANEL ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 650));
        sidebar.setBackground(new Color(18, 18, 18));
        sidebar.setBorder(new EmptyBorder(30, 15, 30, 15));

        // Sidebar Header
        JLabel lblLogo = new JLabel("BOOK MY STAY");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(GOLD);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblLogo);
        
        JLabel lblLogoSub = new JLabel("L U X U R Y   H O T E L");
        lblLogoSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblLogoSub.setForeground(TEXT_MUTED);
        lblLogoSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblLogoSub);

        sidebar.add(Box.createRigidArea(new Dimension(0, 50)));

        // Navigation Menu Buttons
        btnDash = createNavButton("Dashboard", "dash");
        btnBook = createNavButton("Book Room", "book");
        btnManage = createNavButton("Manage Stays", "manage");
        btnAnalytics = createNavButton("Analytics", "analytics");

        sidebar.add(btnDash);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(btnBook);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(btnManage);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(btnAnalytics);

        mainPanel.add(sidebar, BorderLayout.WEST);

        // --- 2. RIGHT CARD LAYOUT CONTENT PANEL ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Create separate Views
        JPanel dashView = createDashboardView();
        JPanel bookView = createBookingView();
        JPanel manageView = createManagementView();
        JPanel analyticsView = createAnalyticsView();

        contentPanel.add(dashView, "dash");
        contentPanel.add(bookView, "book");
        contentPanel.add(manageView, "manage");
        contentPanel.add(analyticsView, "analytics");

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Set Default Active Button
        setActiveNavButton(btnDash);
    }

    private JButton createNavButton(String text, final String cardName) {
        final JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(190, 45));
        btn.setFont(FONT_HEADER);
        btn.setForeground(TEXT_LIGHT);
        btn.setBackground(new Color(30, 30, 30));
        btn.setBorder(new LineBorder(new Color(50, 50, 50), 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) {
                    btn.setBackground(new Color(50, 50, 50));
                    btn.setBorder(new LineBorder(GOLD, 1));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) {
                    btn.setBackground(new Color(30, 30, 30));
                    btn.setBorder(new LineBorder(new Color(50, 50, 50), 1));
                }
            }
        });

        btn.addActionListener(e -> {
            setActiveNavButton(btn);
            cardLayout.show(contentPanel, cardName);
        });

        return btn;
    }

    private void setActiveNavButton(JButton btn) {
        if (activeBtn != null) {
            activeBtn.setBackground(new Color(30, 30, 30));
            activeBtn.setForeground(TEXT_LIGHT);
            activeBtn.setBorder(new LineBorder(new Color(50, 50, 50), 1));
        }
        activeBtn = btn;
        activeBtn.setBackground(GOLD);
        activeBtn.setForeground(Color.BLACK);
        activeBtn.setBorder(new LineBorder(GOLD, 1));
    }

    // --- CREATE MODULE PANELS ---

    // 1. Dashboard View
    private JPanel createDashboardView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BG_DARK);

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerPanel.setBackground(BG_DARK);
        JLabel lblTitle = new JLabel("Welcome back, Administrator");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_LIGHT);
        headerPanel.add(lblTitle);

        JLabel lblSub = new JLabel("Real-time occupancy status and reservation analytics summary.");
        lblSub.setFont(FONT_BODY);
        lblSub.setForeground(TEXT_MUTED);
        headerPanel.add(lblSub);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Stats Cards Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setBackground(BG_DARK);
        statsPanel.setPreferredSize(new Dimension(750, 150));

        lblTotalBookingsVal = new JLabel("0", JLabel.CENTER);
        JPanel cardTotal = createStatsCard("TOTAL BOOKINGS", lblTotalBookingsVal, new Color(54, 185, 204));

        lblActiveStaysVal = new JLabel("0", JLabel.CENTER);
        JPanel cardActive = createStatsCard("ACTIVE GUESTS", lblActiveStaysVal, new Color(28, 200, 138));

        lblRevenueVal = new JLabel("$0.00", JLabel.CENTER);
        JPanel cardRevenue = createStatsCard("TOTAL REVENUE", lblRevenueVal, GOLD);

        lblOccupancyVal = new JLabel("0%", JLabel.CENTER);
        JPanel cardOccupancy = createStatsCard("OCCUPANCY RATE", lblOccupancyVal, new Color(246, 194, 62));

        statsPanel.add(cardTotal);
        statsPanel.add(cardActive);
        statsPanel.add(cardRevenue);
        statsPanel.add(cardOccupancy);
        panel.add(statsPanel, BorderLayout.CENTER);

        // Quick Promo Banner Panel
        JPanel promoCard = new JPanel(new BorderLayout());
        promoCard.setBackground(CARD_BG);
        promoCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(GOLD, 1),
                new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel promoTitle = new JLabel("Premium Hospitality Services");
        promoTitle.setFont(FONT_HEADER);
        promoTitle.setForeground(GOLD);
        promoCard.add(promoTitle, BorderLayout.NORTH);

        JLabel promoText = new JLabel("<html><body>BookMyStay guarantees top-tier performance database validations for seamless hotel management.<br>" +
                "Easily coordinate guest check-ins, resolve booking date conflicts, and keep dynamic reports on room assignments automatically.</body></html>");
        promoText.setFont(FONT_BODY);
        promoText.setForeground(TEXT_LIGHT);
        promoText.setBorder(new EmptyBorder(10, 0, 0, 0));
        promoCard.add(promoText, BorderLayout.CENTER);

        panel.add(promoCard, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatsCard(String titleText, JLabel valLabel, Color AccentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 60), 1),
                new EmptyBorder(20, 15, 20, 15)
        ));

        JLabel lblTitle = new JLabel(titleText, JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(AccentColor);
        card.add(lblTitle, BorderLayout.NORTH);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLabel.setForeground(TEXT_LIGHT);
        card.add(valLabel, BorderLayout.CENTER);

        return card;
    }

    // 2. Booking View Form
    private JPanel createBookingView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BG_DARK);

        JLabel lblTitle = new JLabel("New Guest Reservation Form");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(GOLD);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Form Container Panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 60), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weightx = 1.0;

        // Column 1: Guest Information
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(createFormLabel("Guest Full Name"), gbc);
        txtName = createFormTextField("e.g. Sarvesh Kumar");
        gbc.gridy = 1;
        form.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        form.add(createFormLabel("Email Address"), gbc);
        txtEmail = createFormTextField("e.g. sarvesh@example.com");
        gbc.gridy = 3;
        form.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        form.add(createFormLabel("Phone Number"), gbc);
        txtPhone = createFormTextField("e.g. +91 9876543210");
        gbc.gridy = 5;
        form.add(txtPhone, gbc);

        // Column 2: Booking details
        gbc.gridx = 1; gbc.gridy = 0;
        form.add(createFormLabel("Check-In Date (YYYY-MM-DD)"), gbc);
        
        // Pre-populate date for convenience
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtCheckIn = createFormTextField(sdf.format(new Date()));
        gbc.gridy = 1;
        form.add(txtCheckIn, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        form.add(createFormLabel("Check-Out Date (YYYY-MM-DD)"), gbc);
        
        Date tomorrow = new Date(System.currentTimeMillis() + 86400000L);
        txtCheckOut = createFormTextField(sdf.format(tomorrow));
        gbc.gridy = 3;
        form.add(txtCheckOut, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        form.add(createFormLabel("Select Room (Available Rooms)"), gbc);
        comboRooms = new JComboBox<>();
        comboRooms.setFont(FONT_BODY);
        comboRooms.setBackground(new Color(30, 30, 30));
        comboRooms.setForeground(TEXT_LIGHT);
        comboRooms.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        gbc.gridy = 5;
        form.add(comboRooms, gbc);

        // Row 6: Total Price Estimator
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setBackground(CARD_BG);
        
        JLabel lblPriceName = new JLabel("ESTIMATED VALUE: ");
        lblPriceName.setFont(FONT_HEADER);
        lblPriceName.setForeground(TEXT_MUTED);
        pricePanel.add(lblPriceName);

        lblCalculatedPrice = new JLabel("$0.00", JLabel.LEFT);
        lblCalculatedPrice.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblCalculatedPrice.setForeground(GOLD);
        pricePanel.add(lblCalculatedPrice);

        JButton btnCalc = new JButton("Calculate Price");
        btnCalc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCalc.setBackground(new Color(50, 50, 50));
        btnCalc.setForeground(GOLD);
        btnCalc.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCalc.addActionListener(e -> calculateTotalPriceAndLoadRooms());
        pricePanel.add(Box.createRigidArea(new Dimension(20, 0)));
        pricePanel.add(btnCalc);

        form.add(pricePanel, gbc);

        // Row 7: Action Button
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 15, 5, 15);
        JButton btnSubmit = new JButton("CONFIRM RESERVATION");
        btnSubmit.setFont(FONT_HEADER);
        btnSubmit.setBackground(GOLD);
        btnSubmit.setForeground(Color.BLACK);
        btnSubmit.setBorder(new LineBorder(GOLD, 2));
        btnSubmit.setPreferredSize(new Dimension(0, 45));
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.addActionListener(e -> processReservationSubmit());
        form.add(btnSubmit, gbc);

        panel.add(form, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(GOLD);
        return lbl;
    }

    private JTextField createFormTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setFont(FONT_BODY);
        txt.setBackground(new Color(30, 30, 30));
        txt.setForeground(TEXT_LIGHT);
        txt.setCaretColor(TEXT_LIGHT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(60, 60, 60), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return txt;
    }

    // 3. Stays/Reservation Management View
    private JPanel createManagementView() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_DARK);

        JLabel lblTitle = new JLabel("Reservation Management Panel");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(GOLD);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Scrollable JTable for bookings
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("ID");
        tableModel.addColumn("Guest Name");
        tableModel.addColumn("Room");
        tableModel.addColumn("Type");
        tableModel.addColumn("Check-In");
        tableModel.addColumn("Check-Out");
        tableModel.addColumn("Cost ($)");
        tableModel.addColumn("Status");

        resTable = new JTable(tableModel);
        resTable.setFont(FONT_BODY);
        resTable.setBackground(CARD_BG);
        resTable.setForeground(TEXT_LIGHT);
        resTable.setRowHeight(35);
        resTable.setSelectionBackground(GOLD);
        resTable.setSelectionForeground(Color.BLACK);
        resTable.setGridColor(new Color(60, 60, 60));
        resTable.getTableHeader().setBackground(new Color(18, 18, 18));
        resTable.getTableHeader().setForeground(GOLD);
        resTable.getTableHeader().setFont(FONT_HEADER);
        resTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Apply dark row renderer
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(GOLD);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(52, 52, 52));
                    c.setForeground(TEXT_LIGHT);
                }
                setBorder(new EmptyBorder(5, 10, 5, 10));
                
                // Color status column nicely
                if (column == 7) {
                    String status = String.valueOf(value);
                    if ("Booked".equals(status)) c.setForeground(new Color(54, 185, 204));
                    else if ("CheckedIn".equals(status)) c.setForeground(new Color(28, 200, 138));
                    else if ("CheckedOut".equals(status)) c.setForeground(GOLD);
                    else if ("Cancelled".equals(status)) c.setForeground(new Color(231, 74, 59));
                }
                return c;
            }
        };
        for (int i = 0; i < resTable.getColumnCount(); i++) {
            resTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scroll = new JScrollPane(resTable);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        panel.add(scroll, BorderLayout.CENTER);

        // Control Buttons Panel
        JPanel controls = new JPanel(new GridLayout(1, 3, 20, 0));
        controls.setBackground(BG_DARK);
        controls.setPreferredSize(new Dimension(0, 45));

        JButton btnCheckIn = createActionButton("GUEST CHECK-IN", new Color(28, 200, 138));
        btnCheckIn.addActionListener(e -> handleStatusChange("CheckedIn"));

        JButton btnCheckOut = createActionButton("GUEST CHECK-OUT", GOLD);
        btnCheckOut.addActionListener(e -> handleStatusChange("CheckedOut"));

        JButton btnCancel = createActionButton("CANCEL RESERVATION", new Color(231, 74, 59));
        btnCancel.addActionListener(e -> handleStatusChange("Cancelled"));

        controls.add(btnCheckIn);
        controls.add(btnCheckOut);
        controls.add(btnCancel);
        panel.add(controls, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_HEADER);
        btn.setBackground(bg);
        btn.setForeground(bg == GOLD || bg.equals(new Color(28, 200, 138)) || bg.equals(GOLD) ? Color.BLACK : TEXT_LIGHT);
        btn.setBorder(new LineBorder(bg, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // 4. Analytics Panel View (Custom painted graphs)
    private JPanel createAnalyticsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BG_DARK);

        JLabel lblTitle = new JLabel("System Occupancy & Revenue Analysis");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(GOLD);
        panel.add(lblTitle, BorderLayout.NORTH);

        chartPanel = new ChartPanel();
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    // --- REFRESH AND ACTION LOGIC ---

    private void refreshAllData() {
        // Load stats
        Map<String, Object> stats = dbHelper.getStatistics();
        lblTotalBookingsVal.setText(String.valueOf(stats.get("totalBookings")));
        lblActiveStaysVal.setText(String.valueOf(stats.get("activeStays")));
        lblRevenueVal.setText(String.format("$%,.2f", (Double) stats.get("totalRevenue")));
        lblOccupancyVal.setText(stats.get("occupancyRate") + "%");

        // Load reservations JTable
        tableModel.setRowCount(0);
        List<Map<String, Object>> reservations = dbHelper.getAllReservations();
        for (Map<String, Object> res : reservations) {
            tableModel.addRow(new Object[]{
                    res.get("id"),
                    res.get("guest_name"),
                    res.get("room_number"),
                    res.get("room_type"),
                    res.get("check_in"),
                    res.get("check_out"),
                    res.get("total_price"),
                    res.get("status")
            });
        }

        // Refresh Chart Panel
        Map<String, Integer> distribution = dbHelper.getRoomTypeBookingDistribution();
        chartPanel.setData(distribution);
        
        // Estimate price and load rooms for forms
        calculateTotalPriceAndLoadRooms();
    }

    private void calculateTotalPriceAndLoadRooms() {
        String checkInStr = txtCheckIn.getText().trim();
        String checkOutStr = txtCheckOut.getText().trim();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            Date checkIn = sdf.parse(checkInStr);
            Date checkOut = sdf.parse(checkOutStr);

            if (checkOut.before(checkIn) || checkOut.equals(checkIn)) {
                lblCalculatedPrice.setText("Checkout date must succeed checkin!");
                comboRooms.removeAllItems();
                return;
            }

            long diff = checkOut.getTime() - checkIn.getTime();
            int nights = (int) (diff / (1000 * 60 * 60 * 24));

            // Load available rooms list from database
            comboRooms.removeAllItems();
            availableRoomsList = dbHelper.getAllRooms();
            
            // Loop through all rooms and filter by availability
            int availableCount = 0;
            for (Map<String, Object> room : availableRoomsList) {
                int roomId = (Integer) room.get("id");
                boolean available = dbHelper.isRoomAvailable(roomId, checkInStr, checkOutStr);
                if (available) {
                    double roomPrice = (Double) room.get("price");
                    double totalRoomPrice = roomPrice * nights;
                    String roomDisplay = String.format("Room %s - %s ($%,.2f/night, Total: $%,.2f)",
                            room.get("number"), room.get("type"), roomPrice, totalRoomPrice);
                    comboRooms.addItem(roomDisplay);
                    availableCount++;
                }
            }

            if (availableCount == 0) {
                lblCalculatedPrice.setText("NO ROOMS AVAILABLE");
            } else {
                updateSelectedPriceDisplay(nights);
            }

        } catch (ParseException e) {
            lblCalculatedPrice.setText("Use YYYY-MM-DD formatting!");
            comboRooms.removeAllItems();
        }
    }

    private void updateSelectedPriceDisplay(int nights) {
        int selectedIdx = comboRooms.getSelectedIndex();
        if (selectedIdx >= 0 && availableRoomsList != null) {
            // Find which room was selected
            // We need to sync with the indices of available rooms
            String selectedText = (String) comboRooms.getSelectedItem();
            if (selectedText != null) {
                String roomNum = selectedText.split(" ")[1];
                for (Map<String, Object> room : availableRoomsList) {
                    if (room.get("number").equals(roomNum)) {
                        double price = (Double) room.get("price") * nights;
                        lblCalculatedPrice.setText(String.format("$%,.2f (%d nights)", price, nights));
                        break;
                    }
                }
            }
        }
    }

    private void processReservationSubmit() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String checkInStr = txtCheckIn.getText().trim();
        String checkOutStr = txtCheckOut.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all guest fields.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedIdx = comboRooms.getSelectedIndex();
        if (selectedIdx < 0) {
            JOptionPane.showMessageDialog(this, "No available room selected.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedText = (String) comboRooms.getSelectedItem();
        String roomNum = selectedText.split(" ")[1];
        Map<String, Object> selectedRoom = null;
        for (Map<String, Object> room : availableRoomsList) {
            if (room.get("number").equals(roomNum)) {
                selectedRoom = room;
                break;
            }
        }

        if (selectedRoom == null) return;

        int roomId = (Integer) selectedRoom.get("id");
        double roomPricePerNight = (Double) selectedRoom.get("price");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date checkIn = sdf.parse(checkInStr);
            Date checkOut = sdf.parse(checkOutStr);
            long diff = checkOut.getTime() - checkIn.getTime();
            int nights = (int) (diff / (1000 * 60 * 60 * 24));
            double totalPrice = roomPricePerNight * nights;

            boolean success = dbHelper.createReservation(name, email, phone, roomId, 
                    checkInStr, checkOutStr, totalPrice);

            if (success) {
                JOptionPane.showMessageDialog(this, "Reservation Confirmed successfully for " + name + "!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                // Clear fields
                txtName.setText("");
                txtEmail.setText("");
                txtPhone.setText("");
                
                // Refresh
                refreshAllData();
                cardLayout.show(contentPanel, "dash");
                setActiveNavButton(btnDash);
            } else {
                JOptionPane.showMessageDialog(this, "Database error: unable to create reservation.", 
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing reservation dates.", 
                    "Date Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleStatusChange(String newStatus) {
        int selectedRow = resTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reservation record from the table first.", 
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 7);

        if (currentStatus.equals(newStatus)) {
            JOptionPane.showMessageDialog(this, "Reservation is already in state: " + newStatus, 
                    "No Change Needed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean success = dbHelper.updateReservationStatus(resId, newStatus);
        if (success) {
            JOptionPane.showMessageDialog(this, "Reservation status updated to: " + newStatus, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAllData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update reservation status in database.", 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- CHART GRAPHICS PANEL ---
    class ChartPanel extends JPanel {
        private Map<String, Integer> data;

        public ChartPanel() {
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(60, 60, 60), 1),
                    new EmptyBorder(25, 25, 25, 25)
            ));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int padding = 50;
            int chartWidth = width - 2 * padding;
            int chartHeight = height - 2 * padding;

            // Draw axis lines
            g2.setColor(new Color(80, 80, 80));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(padding, height - padding, width - padding, height - padding); // X axis
            g2.drawLine(padding, padding, padding, height - padding); // Y axis

            if (data == null || data.isEmpty()) {
                g2.setFont(FONT_HEADER);
                g2.setColor(TEXT_MUTED);
                g2.drawString("No analytical reservation records found.", width / 2 - 120, height / 2);
                return;
            }

            // Find max value in map values
            int maxVal = 0;
            for (int val : data.values()) {
                if (val > maxVal) maxVal = val;
            }
            if (maxVal == 0) maxVal = 1;

            // Draw Bar details
            int numBars = data.size();
            int barWidth = chartWidth / (numBars * 2);
            int barGap = barWidth;

            int i = 0;
            Color[] colors = { GOLD, new Color(175, 140, 45), new Color(130, 100, 30) };
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int val = entry.getValue();
                int barHeight = (int) (((double) val / maxVal) * (chartHeight - 40));
                
                int x = padding + barGap/2 + i * (barWidth + barGap);
                int y = height - padding - barHeight;

                // Shadow
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRect(x + 4, y + 4, barWidth, barHeight);

                // Bar paint gradient
                GradientPaint gp = new GradientPaint(x, y, colors[i % colors.length], 
                        x, y + barHeight, colors[i % colors.length].darker());
                g2.setPaint(gp);
                g2.fillRect(x, y, barWidth, barHeight);

                // Values above bars
                g2.setColor(TEXT_LIGHT);
                String valStr = String.valueOf(val);
                int valWidth = g2.getFontMetrics().stringWidth(valStr);
                g2.drawString(valStr, x + barWidth/2 - valWidth/2, y - 10);

                // Labels below axis
                g2.setColor(GOLD);
                String labelStr = entry.getKey();
                int labelWidth = g2.getFontMetrics().stringWidth(labelStr);
                g2.drawString(labelStr, x + barWidth/2 - labelWidth/2, height - padding + 22);

                i++;
            }
            
            // Draw Chart Header Label
            g2.setFont(FONT_HEADER);
            g2.setColor(TEXT_LIGHT);
            g2.drawString("RESERVATIONS BY ROOM CATEGORY TYPE", padding, padding - 15);
        }
    }
}
