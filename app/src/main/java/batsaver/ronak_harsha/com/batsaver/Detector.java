package batsaver.ronak_harsha.com.batsaver;


public class Detector {

    private static final int ACCEL_BUFF_SIZE = 50;
    private static final int VEL_BUFF_SIZE = 10;
    private static final float THRESHOLD = 50f;
    private static final int STEP_DELAY = 250000000;
    private float[] accelX = new float[ACCEL_BUFF_SIZE];
    private float[] accelY = new float[ACCEL_BUFF_SIZE];
    private float[] accelZ = new float[ACCEL_BUFF_SIZE];
    private float[] velBuff = new float[VEL_BUFF_SIZE];
    private int accelCounter = 0;
    private int velCounter = 0;
    private long lastSteptNs = 0;
    private float prevVelEst = 0;
    float velEst;
    float[] meanAccel = new float[3];
    float[] currAccel = new float[3];
    float currVelVec;
    CalorieCalculator calorieCalculator;

   private Listener listener;

    public void registerListener(Listener listener) {
        this.listener = listener;
    }


    public void updateAccelReading(long tNs, float x, float y, float z) {

        float sumX = 0;
        float sumY = 0;
        float sumZ = 0;
        float velSum = 0;
        float normFact = 0;

        currAccel[0] = x;
        currAccel[1] = y;
        currAccel[2] = z;
        
        accelCounter++;
        accelX[accelCounter % ACCEL_BUFF_SIZE] = currAccel[0];
        accelY[accelCounter % ACCEL_BUFF_SIZE] = currAccel[1];
        accelZ[accelCounter % ACCEL_BUFF_SIZE] = currAccel[2];

        for(int i = 0;i < accelX.length;i++)
            sumX += accelX[i];


        for(int i = 0;i < accelY.length;i++)
            sumY += accelY[i];


        for(int i = 0;i < accelZ.length;i++)
            sumZ += accelZ[i];

        
        meanAccel[0] = sumX / Math.min(accelCounter, ACCEL_BUFF_SIZE);
        meanAccel[1] = sumY / Math.min(accelCounter, ACCEL_BUFF_SIZE);
        meanAccel[2] = sumZ / Math.min(accelCounter, ACCEL_BUFF_SIZE);

        for (int i = 0; i < meanAccel.length; i++)
            normFact += meanAccel[i] * meanAccel[i];
        normFact = (float)Math.sqrt(normFact);


        meanAccel[0] /= normFact;
        meanAccel[1] /= normFact;
        meanAccel[2] /= normFact;

        currVelVec = meanAccel[0]*currAccel[0] + meanAccel[1]*currAccel[1] +
                     meanAccel[2]*currAccel[2] - normFact;


        velCounter++;
        velBuff[velCounter % VEL_BUFF_SIZE] = currVelVec;

        for(int i = 0;i < velBuff.length;i++)
            velSum += velBuff[i];

        velEst = velSum;

        if (velEst > THRESHOLD && prevVelEst <= THRESHOLD
                && (tNs - lastSteptNs > STEP_DELAY)) {
            listener.calculateNumSteps(tNs);
            lastSteptNs = tNs;
        }
        prevVelEst = velEst;
    }

}