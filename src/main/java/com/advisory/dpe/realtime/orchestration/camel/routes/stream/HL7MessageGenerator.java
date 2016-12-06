package com.advisory.dpe.realtime.orchestration.camel.routes.stream;

public class HL7MessageGenerator {
    public static final char CARRIAGE_RETURN = 13;

    public static byte[] generateMLLPMessage() {
        StringBuffer msg =
                new StringBuffer()
                        .append(MSH())
                        .append(CARRIAGE_RETURN)
                        .append(SCH())
                        .append(CARRIAGE_RETURN)
                        .append(PID())
                        .append(CARRIAGE_RETURN)
                        .append(AIL())
                        .append(CARRIAGE_RETURN)
                        .append(AIP())
                        .append(CARRIAGE_RETURN);

        return msg.toString().getBytes();
    }

    private static String MSH() {
        return "MSH|^~\\&|eCW Flat-File|unspecified-member-feed|PilotFish|DataHub|201604181646|SIU|SIU^S12|+47611||2.3";
    }

    private static String SCH() {
        return "SCH|+47618|+47618||||^unknown event-reason|unknown appointment-reason|unknown appointment-type|15|MIN|^^^20001006111500|||||||||unknown entered-by-person|||||Pending";
    }

    private static String PID() {
        return "PID|1||ecw_apfm-555||DOE^JOHN||19001029|Male|||100 OAK BLVD^SUITE 100^North Austin^TX^78700||(555)555-1212|||||||||||||||||N";
    }

    private static String AIP() {
        return "AIP|1|ecw_apfm-333^Neil Chop, MD";
    }

    private static String AIL() {
        return "AIL|1|ecw_apfm-8";
    }
}
