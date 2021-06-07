package com.company;

import jssc.SerialPortEvent;
import jssc.SerialPortException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static java.lang.Thread.sleep;

public class DongleLora extends LiaisonSerie {

    private final int VITESSE = 19200;
    private final int DONNEE = 8;
    private final int PARITE = 0;
    private final int STOP = 1;
    String portDeTravail, valeurRetour;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public DongleLora(String portDeTravail) throws SerialPortException {
        this.portDeTravail = portDeTravail;
        initCom();

    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        super.serialEvent(event);
        valeurRetour=new String(lireTrame(event.getEventValue()));
        //pour linux
        System.out.println(ANSI_YELLOW + valeurRetour + ANSI_RESET);

    }

    /*public void serialEvent(SerialPortEvent event) {
            // Instanciation et lancement du traitement
            int nbs = detecteSiReception();
            valeurRetour = new String(lireTrame(nbs));
            //pour windows
            //System.out.println("| Dongle lora-->"+ valeurRetour);
            //pour linux
            System.out.println(ANSI_YELLOW + valeurRetour + ANSI_RESET);
    }*/

    public void initCom() throws SerialPortException {
        System.out.println(listerLesPorts());
        initCom(this.portDeTravail);
        configurerParametres(VITESSE, DONNEE, PARITE, STOP);
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("connecte sur le port " + this.portDeTravail);
        execute_cmd("+++");
        //execute_cmd("ATR+\r");
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        execute_cmd("+++");
    }

    public boolean deconnexion() {
        System.out.println("port " + this.portDeTravail + " deconnecte");
        execute_cmd("ATQ\r"); //fin cmd at
        try {
            sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return fermerPort();
    }

    //dragino parametre
    public void initRadioLora(){
        try {
            execute_cmd("ATO083=01\r");
            sleep(1000);
            execute_cmd("ATM000=01\r");//tx->SF7, rx->SF7
            sleep(1000);
            execute_cmd("ATO075=2D\r");//tx->SF12, rx->SF12
            sleep(1000);
            execute_cmd("ATO076=01\r");//14dbm
            sleep(1000);
            execute_cmd("ATO077=00\r");//tx=rx->868.1MHz
            sleep(1000);
            execute_cmd("ATO081=00\r");//pas de surcharge MAC
            sleep(1000);
            execute_cmd("ATO080=00\r");//unconfirmed
            sleep(1000);
            //execute_cmd("ATS030=08\r");//longeur du preambule=8
            //sleep(1000);
            execute_cmd("ATO086=00\r"); //the cycle will be 4 tries SF7 and 4 SF12.
            sleep(1000);
            execute_cmd("ATO082=32\r");//unconfirmed
            } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void lireInfoConfigOTAA(){
        try {
            System.out.println("DEVICE_CLASS");
            execute_cmd("ATO200\r");
            sleep(1000);
            System.out.println("NET_ID");
            execute_cmd("ATO203\r");//tx->SF7, rx->SF7
            sleep(1000);
            System.out.println("DEV_ADDR");
            execute_cmd("ATO204\r");//pas adaptation de Datarate
            sleep(1000);
            System.out.println("NWK_SKEY");
            execute_cmd("ATO205\r");//tx=rx->868.1MHz
            sleep(1000);
            System.out.println("APP_SKEY");
            execute_cmd("ATO206\r");//tx=rx->868.1MHz
            sleep(1000);
            System.out.println("NETWORK_JOINED");
            execute_cmd("ATO201\r");//tx=rx->868.1MHz
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void initOTAA(String APPT_EUI, String APPT_KEY) {


        //Lecture Device EUI
        System.out.println("Device EUI (lsb)-> ");
        execute_cmd("ATO070\r");
        //Ecriture APPT_EUI(lsb) obligatoire en otaa
        System.out.println("Application EUI (lsb)-> ");
        execute_cmd(String.format("ATO071=%s\r", APPT_EUI));
        //ecriture APPT_KEY=59C4D0E880538BC1EC9BDAB87B9638AA (MsB->lcB) obligatoire en otaa
        System.out.println("App Key (msb)-> ");
        execute_cmd(String.format("ATO072=%s\r", APPT_KEY));

        //force identification OTAA
        System.out.println("Passage en mode OTAA");

        // si drogino
            //initRadioLora();

        //pas dragino
            execute_cmd("ATO083=37\r");


    }

    public void initABP(String APPT_EUI, String APPT_KEY, String NWKS_KEY, String APPS_KEY, String DEV_ADR) {
        //lecture APPT_EUIS=70b3d59ba0000014
        System.out.println("Device EUI (lsb)-> ");
        ecrire("ATO070\r".getBytes());
        //ecriture APPT_EUI(lsb) obligatoire en otaa
        System.out.println("Application EUI (lsb)-> ");
        execute_cmd(String.format("ATO071=%s\r", APPT_EUI));
        //ecriture APPT_KEY=59C4D0E880538BC1EC9BDAB87B9638AA (MsB->lcB) obligatoire en otaa
        System.out.println("App Key (msb)-> ");
        execute_cmd(String.format("ATO072=%s\r", APPT_KEY));
        //ecriture NWKS_KEY
        System.out.println("NwkSKey-> ");
        execute_cmd(String.format("ATO073=%s\r", NWKS_KEY));
        //ecriture APPS_KEY
        System.out.println("AppSKey-> ");
        execute_cmd(String.format("ATO074=%s\r", APPS_KEY));
        //ecrire le dev adresse
        System.out.println("Device adress(lsb)-> ");
        execute_cmd(String.format("ATO069=%s\r", DEV_ADR));
        //force identification ABP
        System.out.println("Passage en mode ABP");
        ecrire("ATO083=036\r".getBytes());
    }

    public void execute_cmd(String cmd) {
        try {
            ecrire(cmd.getBytes(StandardCharsets.UTF_8));
            System.out.println(cmd);
            sleep(2500);
        } catch (InterruptedException e) {

        }
    }

    public void ecrirePayLoad(String payload) {
        System.out.println("Ecriture du payload-> " + payload);
        execute_cmd(String.format("AT$SF=%s\r", payload));
    }

    public void ecrirePayLoadToASCII(String payload) {
        System.out.println("Ecriture du payload-> " + payload);
        byte[] bytes = new byte[0];
        try {
            bytes = payload.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String stringASCII=javax.xml.bind.DatatypeConverter.printHexBinary(bytes);
        System.out.println(stringASCII);
        execute_cmd(String.format("AT$SF=%s\r", stringASCII));
    }

    public String msbToLsb(String keyOuEui) {
        int indexMaxi = keyOuEui.length();
        char[] tableauMsb = keyOuEui.toCharArray();
        char[] tableauLsb = new char[indexMaxi];
        for (int i = 0; i < (indexMaxi); i = i + 2) {
            tableauLsb[i] = tableauMsb[indexMaxi - 2 - i];
            tableauLsb[i + 1] = tableauMsb[indexMaxi - 1 - i];
        }
        return new String(tableauLsb);
    }

}




