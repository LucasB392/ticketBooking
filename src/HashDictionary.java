// Lucas Brown
// 251162440
// October 26, 2024
// CS 2210

import java.util.LinkedList;

public class HashDictionary implements DictionaryADT {

    private LinkedList<Data>[] hashDict; // Array of linked lists to handle collisions
    private int size; // Size of the hash table array

    // Constructor to initialize the hash table with a specified size
    @SuppressWarnings("unchecked") // Suppress unchecked warning for generic array creation
    public HashDictionary(int size) {
        this.size = size;
        this.hashDict = new LinkedList[size];
        
        // Set up each index in the array with an empty LinkedList
        for (int i = 0; i < size; i++) {
            hashDict[i] = new LinkedList<>(); // LinkedList for handling multiple entries at the same index
        }
    }

    // Hash function - calculates hash by summing ASCII values of characters in key, then mods by size to fit array
    private int hash(String key) {
        int hashValue = 0;
        for (char c : key.toCharArray()) {
            hashValue += (int) c; // Adds ASCII value of each character to hashValue
        }
        return hashValue % size; // Mod by array size to get the final index
    }

    // Add a new entry to the hash table
    public int put(Data record) throws DictionaryException {
        int index = hash(record.getConfiguration()); // Determine the index for this entry
        LinkedList<Data> entryList = hashDict[index]; // Access the linked list at that index

        // Check if an entry with the same configuration already exists in this list
        for (int i = 0; i < entryList.size(); i++) {
            Data data = entryList.get(i); // Access current data item in the linked list
            if (data.getConfiguration().equals(record.getConfiguration())) {
                throw new DictionaryException(); // If match is found, throw exception (no duplicates allowed)
            }
        }

        // Add the new entry if it's unique
        entryList.add(record);
        return 0; // Return success code
    }

    // Remove an entry based on its configuration
    public void remove(String config) throws DictionaryException {
        int index = hash(config); // Determine the index for this configuration
        LinkedList<Data> entryList = hashDict[index]; // Access linked list at this index

        // Search through the linked list at this index
        for (int i = 0; i < entryList.size(); i++) {
            Data data = entryList.get(i); // Get current data item
            if (data.getConfiguration().equals(config)) {
                entryList.remove(i); // Remove entry if configuration matches
                return; // Exit once removed
            }
        }

        // If no matching entry is found, throw exception
        throw new DictionaryException();
    }

    // Retrieve score associated with a configuration
    public int get(String config) {
        int index = hash(config); // Get index for this configuration
        LinkedList<Data> entryList = hashDict[index]; // Access linked list at this index

        // Search through the linked list for a matching configuration
        for (int i = 0; i < entryList.size(); i++) {
            Data data = entryList.get(i); // Current data item
            if (data.getConfiguration().equals(config)) {
                return data.getScore(); // Return score if configuration matches
            }
        }

        return -1; // Return -1 if configuration is not found
    }

    // Count total number of records in the dictionary
    public int numRecords() {
        int count = 0;
        for (int i = 0; i < hashDict.length; i++) {
            count += hashDict[i].size(); // Add up the sizes of each linked list
        }
        return count; // Return total number of records
    }
}
