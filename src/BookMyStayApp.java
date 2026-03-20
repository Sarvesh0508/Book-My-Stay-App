import java.io.*;
import java.util.*;

public class BookMyStayApp {

    static class RoomInventory {
        private Map<String, Integer> roomAvailability;

        public RoomInventory() {
            roomAvailability = new HashMap<>();
            roomAvailability.put("Single", 5);
            roomAvailability.put("Double", 3);
            roomAvailability.put("Suite", 2);
        }

        public Map<String, Integer> getRoomAvailability() {
            return roomAvailability;
        }

        public void updateAvailability(String roomType, int count) {
            roomAvailability.put(roomType, count);
        }
    }

    static class FilePersistenceService {

        public void saveInventory(RoomInventory inventory, String filePath) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (Map.Entry<String, Integer> entry : inventory.getRoomAvailability().entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue());
                    writer.newLine();
                }
                System.out.println("Inventory saved successfully.");
            } catch (IOException e) {
                System.out.println("Error saving inventory.");
            }
        }

        public void loadInventory(RoomInventory inventory, String filePath) {
            File file = new File(filePath);

            if (!file.exists()) {
                System.out.println("No valid inventory data found. Starting fresh.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                Map<String, Integer> data = new HashMap<>();

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        data.put(parts[0], Integer.parseInt(parts[1]));
                    }
                }

                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    inventory.updateAvailability(entry.getKey(), entry.getValue());
                }

                System.out.println("Inventory loaded successfully.");

            } catch (Exception e) {
                System.out.println("Error loading inventory. Starting fresh.");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("System Recovery");

        RoomInventory inventory = new RoomInventory();
        FilePersistenceService persistence = new FilePersistenceService();

        String filePath = "inventory.txt";

        persistence.loadInventory(inventory, filePath);

        System.out.println("\nCurrent Inventory:");
        for (Map.Entry<String, Integer> entry : inventory.getRoomAvailability().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        persistence.saveInventory(inventory, filePath);
    }
}