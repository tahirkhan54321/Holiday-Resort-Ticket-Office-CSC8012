import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        SortedArrayList<Activity> sortedActivities = new SortedArrayList<Activity>();
        SortedArrayList<Customer> sortedCustomers = new SortedArrayList<Customer>();
        HashMap<String, Integer> allCustomerActivities = new HashMap<>();
        ArrayList<Activity> activities = new ArrayList<Activity>();
        ArrayList<Customer> customers = new ArrayList<Customer>();
        final int MAXNUMBEROFACTIVITIES = 3;

        try {
            Scanner fileReader = new Scanner(Paths.get("input.txt"));
            int numberOfActivities = Integer.parseInt(fileReader.nextLine());
            /*
            read in activities and add to the activities arraylist
            build a hashmap of activities which can be used in the customer object
             */
            for (int i = 0; i < numberOfActivities; i++) {
                String activityName = fileReader.nextLine();
                int activityTickets = Integer.parseInt(fileReader.nextLine());
                Activity activityElement = new Activity(activityName, activityTickets);
                sortedActivities.addElement(sortedActivities, activityElement);
                allCustomerActivities.put(activityName, 0);
            }
            activities = sortedActivities;

            //Tests
            System.out.println("Number of activities registered: " + numberOfActivities);

            // start populating the customers arraylist
            int numberOfCustomers = Integer.parseInt(fileReader.nextLine());
            /*
              add all customers to the arraylist from the file
             */
            for (int i = 0; i < numberOfCustomers; i++) {
                String line = fileReader.nextLine();
                String fullName[] = line.split(" ");
                String firstName = fullName[0];
                String surname = fullName[1];
                HashMap<String, Integer> eachCustomerActivities = new HashMap<>(allCustomerActivities);
                Customer customer = new Customer(firstName, surname, eachCustomerActivities);
                sortedCustomers.addElement(sortedCustomers, customer);
            }
            customers = sortedCustomers;

            //Tests
            System.out.println("Number of customers registered: " + numberOfCustomers);

        } catch (IOException e) {
            System.out.println("The file does not exist.");
        }

        //tests to see whether all activities/customers are registered to arraylists - pass x2
        System.out.println(activities);
        System.out.println(customers);


        while (true) {
            printCustomerChoices();
            String userChoice = scanner.nextLine();

            if (userChoice.equals("f")) {
                break;
            } else if (userChoice.equals("a")) {
                printActivities(activities);
                System.out.println("");
            } else if (userChoice.equals("c")) {
                printCustomers(customers);
                System.out.println("");
            } else if (userChoice.equals("t") || userChoice.equals("r")) {
                //verifying name exists and creating a confirmed string and object to use in later parts of the program
                System.out.println("What is your full name?");
                String confirmedName = null;
                Customer confirmedCustomer = new Customer(null, null, null);
                String nameLine = scanner.nextLine();
                boolean validName = true;
                try {
                    confirmedName = populateConfirmedCustomerName(nameLine, customers);
                    confirmedCustomer = populateConfirmedCustomer(nameLine, customers);
                    if (confirmedName == null) {
                        System.out.println("Your customer name was invalid. The request has not been processed.");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("You haven't included both a first name and last name separated by a space.");
                    validName = false;
                }
                if (validName == false) {
                    continue;
                }

                //verifying activity exists and creating a confirmed string and object to use in later parts of the program
                System.out.println("What is the name of the activity you'd like to update tickets for?");
                String activityRequested = scanner.nextLine();
                String confirmedActivityName = null;
                Activity confirmedActivity = new Activity(null, 0);
                confirmedActivityName = populateConfirmedActivityName(activities, activityRequested);
                confirmedActivity = populateConfirmedActivity(activities, activityRequested);
                if (confirmedActivityName == null) {
                    System.out.println("Your activity name was invalid. The request has not been processed.");
                    continue;
                }

                if (userChoice.equals("t")) {
                    //t - to update the stored data when tickets are bought by one of the registered customers.
                    //checking if there are enough tickets left for the activity
                    System.out.println("How many tickets would you like to buy?");
                    int numberOfTickets = Integer.valueOf(scanner.nextLine());
                    boolean noOfTicketsBoughtValidity = false;

                    if (numberOfTickets <= 0) {
                        System.out.println("You have input an invalid number of tickets." +
                                "The request has not been processed.");
                        noOfTicketsBoughtValidity = false;
                    } else if (numberOfTickets > 0) {
                        if (confirmedActivity.getmaxNumberOfTicketsAvailable() - numberOfTickets >= 0) {
                            noOfTicketsBoughtValidity = true;
                        } else if (confirmedActivity.getmaxNumberOfTicketsAvailable() - numberOfTickets < 0) {
                            System.out.println("There are not enough tickets available for this activity." +
                                    "The request has not been processed");
                            noOfTicketsBoughtValidity = false;
                        }
                    }

                    //adding the tickets for the activity to the customer if valid
                    if (noOfTicketsBoughtValidity == true && confirmedCustomer.getUniqueActivityCounter() < MAXNUMBEROFACTIVITIES) {
                        confirmedCustomer.boughtTickets(numberOfTickets, activityRequested);
                        confirmedActivity.buyTickets(numberOfTickets);
                        System.out.println("You have bought " + numberOfTickets + " tickets for " +
                                confirmedActivityName + ".");
                    } else if (confirmedCustomer.getUniqueActivityCounter() >= 3) {
                        System.out.println("You can only order tickets for a maximum of three activities. " +
                                "The request has not been processed.");
                    }

                    //print statement to test output
                    System.out.println("activity we bought tickets for: " + confirmedActivity +
                            "customer we added tickets to: " + confirmedCustomer);


                } else if (userChoice.equals("r")) {
                    //r - to update the stored data when a registered customer cancels tickets for a booking

                    /* checking if there are enough tickets left for the activity
                        if each customer can only sell as many tickets as they own and no more,
                        then the aggregate number of tickets for the activity can't exceed the original amount
                        by the act of selling tickets back
                     */
                    System.out.println("How many tickets would you like to cancel?");
                    int numberOfTickets = Integer.valueOf(scanner.nextLine());
                    int ticketsRemainingForCustomer = confirmedCustomer.getActivityMap().get(confirmedActivityName)
                            - numberOfTickets;

                    if (ticketsRemainingForCustomer >= 0) {
                        confirmedCustomer.soldTickets(numberOfTickets, confirmedActivityName);
                        confirmedActivity.sellTickets(numberOfTickets);
                        System.out.println("You have cancelled " + numberOfTickets + " tickets for " +
                                confirmedActivityName + ".");
                    }
                    if (ticketsRemainingForCustomer == 0) {
                        //want to decrement activity uniqueCounter
                        confirmedCustomer.clearedTicketsFromActivity();
                    }
                    if (ticketsRemainingForCustomer < 0) {
                        System.out.println("You have tried to cancel more tickets than you own. " +
                                "The request has not been processed.");
                    }

                    //print statement to test output
                    System.out.println("activity we sold tickets for: " + confirmedActivity +
                            "customer we removed tickets from: " + confirmedCustomer);
                }

            } else {
                System.out.println("You have selected an invalid option, try again");
            }
        }
    }

    private static void printCustomerChoices() {
        //customer choices
        System.out.println("Select an option:");
        System.out.println("f - to finish running the program.");
        System.out.println("a - to display on the screen information about all the activities.");
        System.out.println("c - to display on the screen information about all the customers.");
        System.out.println("t - to update the stored data when tickets are bought by one of the registered customers.");
        System.out.println("r - to update the stored data when a registered customer cancels tickets for a booking.");
    }

    private static void printActivities(ArrayList<Activity> activities) {
        for (Activity activity : activities) {
            System.out.println(activity.toString());
        }
    }

    private static void printCustomers(ArrayList<Customer> customers) {
        for (Customer customer : customers) {
            System.out.println("\n" + customer.getFullName() + " has tickets for these activities:");
            customer.getActivityMap().forEach((activity, tickets) -> System.out.println(activity + ": " + tickets));
            //this way of iterating over a hashmap was taken from stack overflow:
            //https://stackoverflow.com/questions/46898/how-do-i-efficiently-iterate-over-each-entry-in-a-java-map
        }
    }

    private static String populateConfirmedCustomerName(String nameLine, ArrayList<Customer> customers) {
        String fullName[] = nameLine.split(" ");
        String firstName = fullName[0];
        String surname = fullName[1];
        String confirmedName = null;
        for (Customer customer : customers) {  //TODO: can this be replaced with a binary search once the list is sorted?
            if (customer.getFirstName().equals(firstName) && customer.getSurname().equals(surname)) {
                //customer is valid
                confirmedName = fullName + " " + surname;
            }
        }
        return confirmedName;
    }

    private static Customer populateConfirmedCustomer(String nameLine, ArrayList<Customer> customers) {
        String fullName[] = nameLine.split(" ");
        String firstName = fullName[0];
        String surname = fullName[1];
        Customer confirmedCustomer = new Customer(null, null, null);
        for (Customer customer : customers) {  //TODO: can this be replaced with a binary search once the list is sorted?
            if (customer.getFirstName().equals(firstName) && customer.getSurname().equals(surname)) {
                //customer is valid
                confirmedCustomer = customer;
            }
        }
        return confirmedCustomer;
    }

    private static String populateConfirmedActivityName(ArrayList<Activity> activities,
                                                        String activityRequested) {
        String confirmedActivityName = null;
        for (Activity activity : activities) {
            if (activity.getActivityName().equals(activityRequested)) {
                //activity is valid
                confirmedActivityName = activityRequested;
                break;
            }
        }
        return confirmedActivityName;
    }

    private static Activity populateConfirmedActivity(ArrayList<Activity> activities,
                                                      String activityRequested) {
        Activity confirmedActivity = new Activity(null, 0);
        for (Activity activity : activities) {
            if (activity.getActivityName().equals(activityRequested)) {
                //activity is valid
                confirmedActivity = activity;
                break;
            }
        }
        return confirmedActivity;
    }
}