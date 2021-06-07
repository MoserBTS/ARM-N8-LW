package com.company;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import java.util.Base64;


import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws InterruptedException, SerialPortException {
        //DongleLora dongleLora = new DongleLora("/dev/ttyAMA0");
        DongleLora dongleLora = new DongleLora("com13");
        dongleLora.initOTAA(dongleLora.msbToLsb("70B3D59BA0000013"),
                "A70642084D3AFA96752EACEFEA6753CB");


        Thread.sleep(5000);
        // premier ecriture pour effectuer identification

        String message=" given packet design with a payload of 100 bytes fits in a single payload at DR3 through DR7. If the transmitting device is at DR0 through DR2, it will not fit in the allowed maximum payload size. If a system designer can restrict DR limits (for example, never below DR3), the payload specification is more flexible. This choice creates limitations on signal transmission range because the lower the DR, the more processing gain there is to boost receiver sensitivity for uplink packets. Limiting DR to only higher values also limits how far away signals can be received. Consider this when you choose a packet length. Also note that a device designer cannot choose the final DR.";
        while (true) {
            dongleLora.ecrirePayLoadToASCII(message.substring(0,124));
            Thread.sleep(15000);
        }
    }
}
