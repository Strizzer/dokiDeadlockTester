
import java.util.Random;

class doki {

    public class ship extends Thread implements Ship{

        private SeaportManagerImpl managerRef;
        private int size;
        boolean sailedAway;
        private String name;
        private long timeOfArrival;
        private Integer assignedDock;
        private int symSpeed1;
        private int symSpeed2;
        private int symSpeed3;
        private int symSpeed4;
        private int symSpeed5;
        private int symSpeed6;
        private int ships;
        private int randomComponentMax;

        public ship(String Name, SeaportManagerImpl e, int docksize, int syms1, int syms2, int syms3, int syms4, int syms5, int syms6, int randomC, int Ships){
            timeOfArrival = System.currentTimeMillis();
            name = Name;
            ships = Ships;
            sailedAway = true;
            managerRef = e;
            size = docksize;
            assignedDock = null;
            symSpeed1 = syms1;
            symSpeed2 = syms2;
            symSpeed3 = syms3;
            symSpeed4 = syms4;
            symSpeed5 = syms5;
            symSpeed6 = syms6;
            randomComponentMax = randomC;
        }

        public int getDockingSize(){
            return size;
        }

        public Integer getAssignedDock(){
            return assignedDock;
        }

        public void assignDock(Integer dock){
            assignedDock = dock;
        }

        public boolean checkForDeadlock(){
            if( System.currentTimeMillis() - timeOfArrival > (symSpeed1 + symSpeed2 + symSpeed3 + symSpeed4 + symSpeed5 + symSpeed6 + randomComponentMax)*ships){
                return true;
            }
            else
                return false;
        }

        public boolean DoesSailedAway(){
            return sailedAway;
        }

        public void run(){
            sailedAway = false;
            Random r = new Random();
            try{
                sleep(symSpeed1 + r.nextInt(randomComponentMax));
            }catch(InterruptedException e){}

            System.out.println("ship " + name + " requested seaway entrance");
            managerRef.requestSeawayEntrance(this);
            System.out.println("ship " + name + " entered seaway");
            try{
                sleep(symSpeed2 + r.nextInt(randomComponentMax));
            }catch(InterruptedException e){}

            System.out.println("ship " + name + " requested port entrance");
            assignDock( managerRef.requestPortEntrance(this));
            System.out.println("ship " + name + " is entering port ");

            try{
                sleep(symSpeed3 + r.nextInt(randomComponentMax));
            }catch(InterruptedException e){}

            System.out.println("ship " + name + " left seaway");
            managerRef.signalPortEntered(this);
            System.out.println("ship " + name + " entered port");

            try{
                sleep(symSpeed4 + r.nextInt(randomComponentMax));
            }catch(InterruptedException e){}

            System.out.println("ship " + name + " requested port exit");
            managerRef.requestPortExit(this);
            System.out.println("ship " + name + " is entering seaway");

            try{
                sleep(symSpeed5 + r.nextInt(randomComponentMax));
            }catch(InterruptedException e){}

            System.out.println("ship " + name + " left port");
            managerRef.signalPortExited(this);

            try{
                sleep(symSpeed6 + r.nextInt(randomComponentMax));
            }catch(InterruptedException e){}

            
            managerRef.signalShipSailedAway(this);

            sailedAway = true;

            if(checkForDeadlock()){
                System.out.println("ship " + name + " stayed in port for very long time...");
            }else
                System.out.println("ship " + name + " left seaway and is now on the open sea, good work!");

        }
    }

    public static void main(String[] args) {
        int ships = 100;  //sumaryczna ilosc statkow przyplywajacych do portu
        int sp1 = 1000; //jak dlugo statek przyplywa do portu (port czyli cala infrastruktura)
        int sp2 = 1000; //jak dlugi jest kanal jak dlugo statek plynie przez kanal
        int sp3 = 1000; //jak wczesnie statek requestuje wejscie do portu (czas po jakim opusci kanal od wywolania requesta)
        int sp4 = 1000; //jak dlugo statek przebywa w porcie
        int sp5 = 1000; //czas po jakim statek opuszcza port( od wywolania requesta)
        int sp6 = 1000; //jak dlugi jest kanal jak dlugo statek plynie przez kanal
        int rc = 1000;  //poslizg losowy
        //uwaga! zatrzymanie statku w ktorejs z funkcji oznacza ze statek stoi i czeka na decyzję( czasy się zatrzymuja dla danego watku )

        int N = 10;//ilosc dokow
        int maxS = 3;//maksymalny rozmiar statku( minimalny = 1)
        int K = 3;//szerokosc kanalu
        int p = 5; // ilosc parti przyplywajacych statkow


        int sailedAway = 0;
        boolean[] sailed = new boolean[ ships ];
        for(int i = 0; i < ships; i++)
            sailed[ i ] = false;

        SeaportManagerImpl manager = new SeaportManagerImpl();
        Random r = new Random();
        doki handle = new doki();
        ship[] shipArray = new ship[ ships ];

        manager.init(N, K);

        for(int i = 0; i < ships; i++){
            shipArray[ i ] = handle.new ship("#" + String.valueOf(i + 1), manager, 1 + r.nextInt(maxS), sp1, sp2, sp3, sp4, sp5, sp6, rc, ships );
        }

        for(int j = 0; j < p; j++){
            try{
                Thread.sleep(10000);
            }catch(InterruptedException e){}
            for(int i = j*ships/p; i < (j+1)*ships/p; i++){
                try{
                Thread.sleep(100);
                }catch(InterruptedException e){}
                shipArray[ i ].start();
            }
        }

       
        
       while(sailedAway < ships){
           for(int i = 0; i < ships; i++){
                if( sailed[ i ] == false && shipArray[ i ].DoesSailedAway()){
                    sailedAway++;
                    sailed[ i ] = true;
                }else if(sailed[ i ] == false && true == shipArray[ i ].checkForDeadlock()){
                    System.out.println("the crew of ship " + shipArray[ i ].name + " starved to death");
                    sailed[ i ] = true;
                }

           }
       }
       System.out.println("All ships sailed away!");

    }
}