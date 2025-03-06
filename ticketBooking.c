#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>

#define ROWS 5                  // Number of rows in the theater
#define COLUMNS 12              // Number of columns (seats) in the theater
#define MAX_THREADS 1024        // Maximum number of threads allowed

// Global variables
int theater[ROWS][COLUMNS];      // Theater array to store bookings, 0 means available, customer ID means booked
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;  // Mutex for synchronizing access to the theater

/**
 * Prints the current layout of the theater with booked seats.
 */
void print_theater() {
    printf("Final Theater Layout:\n");
    printf("\t");  // Print tab before column numbers
    // Print column numbers (1 to 12)
    for (int i = 1; i < 13; i++) {
        printf("%d   ", i);  
    }
    printf("\n");
    
    // Print each row of the theater
    for (int j = 0; j < ROWS; j++) {
        printf("Aisle %d ", j + 1);  // Print aisle number (1 to 5)
        for (int k = 0; k < COLUMNS; k++) {
            printf("%3d ", theater[j][k]);  // Print seat number or customer ID (booked seat)
        }
        printf("\n");
    }
}

/**
 * Prints the successfully booked seats for a customer.
 * @param customer_id The ID of the customer
 * @param seats The array of aisle and seat numbers booked by the customer
 * @param count The number of seats booked
 */
void print_booked_seats(int customer_id, int seats[12][2], int count) {
    printf("Customer %d successfully booked seats: ", customer_id);
    // Print the booked seats for the customer
    for (int i = 0; i < count; i++) {
        printf("Aisle %d, Seat %d", seats[i][0], seats[i][1]);
        if (i < count - 1) printf(", ");  // Print comma between booked seats
    }
    printf("\n");
}

/**
 * Checks if the given line starts with a number. 
 * Used to skip the first line of input if it is invalid.
 * @param line The string to check
 * @return 1 if the string starts with a number, 0 otherwise
 */
int starts_with_integer(const char *line) {
    if (line == NULL || *line == '\0') return 0;  // Empty line
    return isdigit(*line);  // Check if the first character is a digit
}

/**
 * Attempts to book tickets for a customer. This function is executed by a separate thread for each customer.
 * It tries to book the requested seats and prints the booking result.
 * @param arg The argument passed to the thread (input line from the file)
 * @return NULL on completion
 */
void *buy_tickets(void *arg) {
    char *line = (char *)arg;    // Get the input line from the file
    int seats[12][2];            // Array to store aisle/seat pairs from the input line
    int count = 0;               // Number of seats requested by the customer
    char *saveptr;

    // Get customer ID from the first token in the line
    char *token = strtok_r(line, ",", &saveptr);  
    if (token == NULL) {
        fprintf(stderr, "Error: Invalid input format\n");
        free(arg);
        return NULL;
    }
    int customer_id = atoi(token);  // Convert customer ID to integer

    // Get aisle and seat pairs from the remaining tokens
    while ((token = strtok_r(NULL, ",", &saveptr)) != NULL) {
        if (count >= 12) {
            fprintf(stderr, "Error: Too many seat requests for Customer %d\n", customer_id);
            free(arg);
            return NULL;
        }

        // Aisle number (1-5)
        seats[count][0] = atoi(token); 
        token = strtok_r(NULL, ",", &saveptr);

        // Seat number (1-12)
        if (token == NULL) {
            fprintf(stderr, "Error: Missing seat number for Customer %d\n", customer_id);
            free(arg);
            return NULL;
        }
        seats[count][1] = atoi(token); 
        
        count++;
    }

    // Attempt to book the seats within the critical section
    pthread_mutex_lock(&mutex);  // Lock the critical section to prevent race conditions

    int can_book = 1;  // Assume all seats are available
    for (int i = 0; i < count; i++) {
        int aisle = seats[i][0] - 1;   // Convert aisle number to 0-based index
        int seat = seats[i][1] - 1;    // Convert seat number to 0-based index
        
        // Check if the seat is within valid range
        if (aisle < 0 || aisle >= ROWS || seat < 0 || seat >= COLUMNS) {
            fprintf(stderr, "Error: Invalid seat (Aisle %d, Seat %d) for Customer %d\n", aisle + 1, seat + 1, customer_id);
            can_book = 0;
            break;
        }
        // Check if the seat is already booked
        if (theater[aisle][seat] != 0) {
            can_book = 0;  // Seat already taken
            break;
        }
    }

    if (can_book) {
        // If seats are available, book them
        for (int i = 0; i < count; i++) {
            int aisle = seats[i][0] - 1;
            int seat = seats[i][1] - 1;
            theater[aisle][seat] = customer_id;  // Mark the seat as booked by the customer
        }
        print_booked_seats(customer_id, seats, count);  // Print booked seats
        sleep(rand() % 3 + 1);  // Simulate booking activity with random sleep
    } else {
        printf("Customer %d failed to book seats\n", customer_id);  // Print failure message if booking failed
    }

    pthread_mutex_unlock(&mutex);  // Unlock the critical section
    free(arg);  // Free the allocated memory for the input line
    return NULL;
}

/**
 * Main function to execute the ticket booking system.
 * Reads the input file, processes the bookings using threads, and prints the final theater layout.
 * @param argc Number of arguments
 * @param argv List of arguments (first argument is the input file)
 * @return EXIT_SUCCESS if successful, EXIT_FAILURE otherwise
 */
int main(int argc, char *argv[]) {
    if (argc != 2) {
        fprintf(stderr, "Usage: %s <input_file>\n", argv[0]);
        return EXIT_FAILURE;
    }

    // Open the input file containing booking requests
    FILE *file = fopen(argv[1], "r");
    if (!file) {
        perror("Failed to open file");
        return EXIT_FAILURE;
    }

    srand(time(NULL));  // Seed for random sleep to simulate booking delays

    char line[256];  // Buffer to read each line from the file
    pthread_t threads[MAX_THREADS];  // Array to store thread identifiers
    int thread_count = 0;  // Counter for the number of threads created
    int first_line_skipped = 0;  // Flag to skip the first line if it's not valid

    // Read each line from the file and process it
    while (fgets(line, sizeof(line), file) != NULL) {
        if (line[0] == '\n' || strlen(line) == 0) continue;  // Skip empty lines

        // Skip the first line if it doesn't start with an integer
        if (!first_line_skipped && !starts_with_integer(line)) {
            first_line_skipped = 1;  // Mark first line as skipped
            continue;
        }

        char *hash = strchr(line, '#');
        if (hash) *hash = '\0';  // Remove comment

        if (strlen(line) == 0) continue; // Skip empty lines after removing comments

        // Duplicate the line for thread processing
        char *line_copy = strdup(line);  
        if (!line_copy) {
            perror("Memory allocation failed");
            fclose(file);
            return EXIT_FAILURE;
        }

        if (thread_count >= MAX_THREADS) {
            fprintf(stderr, "Error: Too many threads created\n");
            free(line_copy);
            fclose(file);
            return EXIT_FAILURE;
        }

        // Create a new thread to handle ticket booking for this line
        pthread_create(&threads[thread_count++], NULL, buy_tickets, line_copy);  
    }

    fclose(file);  // Close the input file

    // Join all threads to ensure they finish before printing the final layout
    for (int i = 0; i < thread_count; i++) {
        pthread_join(threads[i], NULL);
    }

    print_theater();  // Print the final theater layout with all bookings

    pthread_mutex_destroy(&mutex);  // Destroy the mutex to clean up resources
    return EXIT_SUCCESS;  // Return success
}
