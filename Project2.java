
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Project2 implements Runnable {

    // lock for synchronize()
    private final Object lock = new Object();
    
    // required arrays
    static String[] movies = new String[5];
    static int[] movieCount = {0, 0, 0, 0, 0};
    static boolean[] movieFull = {false, false, false, false, false};
    static String[] food = {"Popcorn", "Soda", "Popcorn and Soda"};
    static int globalChoice;

    // local variables for each thread
    String type;
    int num;
    int foodChoice;
    int visitConcession = 0;
    int movieChoice = 0;

    // global variables used by all threads
    public static int customerNum = 0;
    public static int boxOfficeNum = 0;
    public static int customerCount = 50;
    public static int customerNumBO = 0;
    public static int customerNumTT = 0;
    public static int customerNumC = 0;
    public static int boxOffice = 2;
    public static int ticketTear = 1;
    public static int concession = 1;
    public static int totalPeople = 54;
    public static int globalFoodChoice = 0;

    // semaphores used by threads to coordinate
    private static Semaphore inTheatre = new Semaphore(50, true);
    private static Semaphore waitingOnBO = new Semaphore(0, true);
    private static Semaphore waitCusBO = new Semaphore(0, true);
    private static Semaphore buyingTicket = new Semaphore(1, true);
    private static Semaphore ticketTearLine = new Semaphore(50, true);
    private static Semaphore seeTicketTearer = new Semaphore(1, true);
    private static Semaphore waitCusTT = new Semaphore(0, true);
    private static Semaphore waitingOnTT = new Semaphore(0, true);
    private static Semaphore concessionLine = new Semaphore(50, true);
    private static Semaphore seeConcessions = new Semaphore(1, true);
    private static Semaphore waitingOnC = new Semaphore(0, true);
    private static Semaphore waitCusC = new Semaphore(0, true);

    // constructor used by non-customer threads
    Project2(String type, int num) {
        this.type = type;
        this.num = num;
    }

    // constructor used by customer threads
    Project2(String type, int num, int foodChoice, int movieChoice, int visitConcession) {
        this.visitConcession = visitConcession;
        this.num = num;
        this.type = type;
        this.movieChoice = movieChoice;
        this.foodChoice = foodChoice;
    }

    // customer thread function
    public void customerFunc() {
        try {
            // enter the threater and get in line to buy a ticket
            inTheatre.acquire();
            buyingTicket.acquire();
            System.out.println("Customer " + this.num + " created, buying ticket to " + movies[this.movieChoice]);
            globalChoice = this.movieChoice;
            customerNumBO = this.num;
            waitCusBO.release();
            waitingOnBO.acquire();

            // if the movie is full, then leave the theater
            if (movieFull[movieChoice]) {
                // continue to the join
            } else {    // else, continue on the ticker tearer, get in line and give them your ticket
                synchronized (lock) {
                    ticketTearLine.acquire();
                    seeTicketTearer.acquire();
                    customerNumTT = this.num;
                    System.out.println("Customer " + this.num + " in line to see Ticker taker");
                    waitCusTT.release();
                    waitingOnTT.acquire();
                }

                // if randomly assigned value is 0, skip concessions, if it is 1, buy randomly assigned concession
                if (this.visitConcession == 0) {
                    // skip it
                } else if (this.visitConcession == 1) {
                    concessionLine.acquire();
                    System.out.println("Customer " + this.num + " in line to buy " + food[this.foodChoice]);
                    seeConcessions.acquire();
                    globalFoodChoice = this.foodChoice;
                    customerNumC = this.num;
                    waitCusC.release();
                    waitingOnC.acquire();
                }

                // go into the theater and see your movie (each movie lasts 4 seconds)
                System.out.println("Customer " + this.num + " enters Theatre to see " + movies[this.movieChoice]);
                switch (this.movieChoice) {
                    case 0:
                        Thread.currentThread().sleep(4000);
                        inTheatre.release();
                        break;
                    case 1:
                        Thread.currentThread().sleep(4000);
                        inTheatre.release();
                        break;
                    case 2:
                        Thread.currentThread().sleep(4000);
                        inTheatre.release();
                        break;
                    case 3:
                        Thread.currentThread().sleep(4000);
                        inTheatre.release();
                        break;
                    case 4:
                        Thread.currentThread().sleep(4000);
                        inTheatre.release();
                        break;
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // box office thread function
    public void boxOfficePerson() {
        System.out.println("Box office agent " + this.num + " created");
        while (customerCount != 0) {
            // get the next customer in line
            try {
                waitCusBO.acquire();
                System.out.println("Box office agent " + this.num + " serving customer " + customerNumBO);
                Thread.currentThread().sleep(1500);

                // check if the movie is full, if it is send the customer away, if it is not, give the customer their ticket
                if (movieCount[globalChoice] == 10) {
                    movieFull[globalChoice] = true;
                    System.out.println("Movie is full, customer " + customerNumBO + " is leaving");
                    buyingTicket.release();
                    waitingOnBO.release();
                } else {
                    System.out.println("Box office agent " + this.num + " sold ticket for " + movies[globalChoice] + " to customer " + customerNumBO);
                    buyingTicket.release();
                    movieCount[globalChoice]++;
                    waitingOnBO.release();
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // ticket tearer thread fucntion
    public void ticketTearer() {
        System.out.println("Ticket taker created");
        while (customerCount != 0) {
            try {
                // get next person in line, take their ticket and give it back
                waitCusTT.acquire();
                Thread.currentThread().sleep(250);
                System.out.println("Ticket taken from Customer " + customerNumTT);
                seeTicketTearer.release();
                waitingOnTT.release();
            } catch (InterruptedException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // concession worker thread function
    public void concessionWorker() {
        System.out.println("Concession stand worker created");
        while (customerCount != 0) {
            try {
                // get next person in the line, wait 3 seconds and give them their food
                waitCusC.acquire();
                Thread.currentThread().sleep(3000);
                System.out.println("Order for " + food[globalFoodChoice] + " given to Customer " + customerNumC);
                waitingOnC.release();
                seeConcessions.release();
            } catch (InterruptedException ex) {
                Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // threads go to their respective functions based on the value assigned to them
    public void run() {
        if (type.compareTo("boxoffice") == 0) {
            boxOfficePerson();
        } else if (type.compareTo("ticket") == 0) {
            ticketTearer();
        } else if (type.compareTo("concession") == 0) {
            concessionWorker();
        } else if (type.compareTo("customer") == 0) {
            customerFunc();
        }
    }

    // read the movie names and max capacity from the movies.txt file, save to movies array
    public static void readFile() {
        String buffer = "";
        String[] input = new String[2];
        try {
            File file = new File("movies.txt");
            Scanner sc = new Scanner(file);
            int i = 0;
            while (sc.hasNextLine()) {
                buffer = sc.nextLine();
                input = buffer.split("\t");
                movies[i] = input[0];
                i++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        // read the input file
        readFile();

        // required objects and object arrays
        Random rand = new Random();
        Project2 cus[] = new Project2[customerCount];
        Project2 box[] = new Project2[boxOffice];
        Project2 ticket[] = new Project2[ticketTear];
        Project2 concess[] = new Project2[concession];

        // array of all threads
        Thread myThread[] = new Thread[totalPeople];

        // c keeps track of total amount of threads created so i dont go over totalpeople
        int c = 0;
        int j = 0;

        // create the box office threads
        for (int i = 0; i < boxOffice; i++) {
            box[i] = new Project2("boxoffice", ++j);
            String name = "boxoffice";
            String rName = name.concat(Integer.toString(i + 1));
            myThread[c] = new Thread(box[i], rName);
            myThread[c].start();
            c++;
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
        }

        // create the ticket tearer threads
        j = 0;
        for (int i = 0; i < ticketTear; i++) {
            ticket[i] = new Project2("ticket", ++j);
            String name = "ticket";
            String rName = name.concat(Integer.toString(i + 1));
            myThread[c] = new Thread(ticket[i], rName);
            myThread[c].start();
            c++;
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
        }

        // create the concession worker threads
        j = 0;
        for (int i = 0; i < concession; i++) {
            concess[i] = new Project2("concession", ++j);
            String name = "concession";
            String rName = name.concat(Integer.toString(i + 1));
            myThread[c] = new Thread(concess[i], rName);
            myThread[c].start();
            c++;
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(Project2.class.getName()).log(Level.SEVERE, null, ex);
        }

        // create the customer threads
        System.out.println("Theater open");
        for (int i = 0; i < customerCount; i++) {
            cus[i] = new Project2("customer", i + 1, rand.nextInt(3), rand.nextInt(5), rand.nextInt(2));
            String name = "customer";
            String rName = name.concat(Integer.toString(i + 1));
            myThread[c] = new Thread(cus[i], rName);
            myThread[c].start();
            c++;
        }

        // join the threads (delete them)
        for (int i = 0; i < totalPeople; i++) {
            try {
                myThread[i].join();
                customerCount--;
                System.out.println("joined Customer " + i);
            } catch (InterruptedException E) {
                System.out.println(E);
            }
        }
    }

}
