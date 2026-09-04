/*programma caricato e aggiornato 3/9/26 11:29
SOFTWARE
aggiunta controllo indipendente open servo,
aggiunta rotazione slowintake motor in intake,
aggiunta rotazione inversa flywheel in outtake,
modoifica utilizzo idle velocity (c'è ancora ma non serve)

HARDWARE
modifica altezza primo rullo intake
aggiunta due pezzi per blocco incastro palline sia buco sopra centrale che buchi laterali flywheel
prima prova slider
 */

/*programma caricato e aggiornato 4/9/26
SOFTWARE
aggiunta controllo unico prima dello sparo a 1700 giri
implementazione di tutte le funzioni sia su godmode che su claudiomode
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * =========================================================================================
 *                                GAMEPAD 1 | GUIDA (DRIVER)
 * =========================================================================================
 *
 *       [L2] Marcia GIÙ (-25%)                                  [R2] Marcia SÙ (+25%)
 *       [L1] Outtake                                            [R1] Intake (Aspirazione)
 *
 *              .---| DPAD |---.                                    .---| TASTI |---.
 *             /      .^.       \                                  /      (Y)        \  Y: [God Mode] Flywheel Full Speed ON/OFF
 *            |     <     >      |                                |    (X)   (B)     | B: Toggle Uster / Arcade (SOLO Claudio Mode)
 *             \      'v'       /                                  \      (A)        /  X: Toggle Climbing
 *              '--------------'                                    '---------------'   A: [God Mode] Sparo Flywheel (se velocityok)
 *             ^: Climb Speed +0.1        [God Mode] dpad_left: Flywheel Idle ON/OFF
 *             v: Climb Speed -0.1
 *
 *                               ( L3 )              ( R3 )
 *                              /      \            /      \
 *                             | L-STICK|          | R-STICK|
 *                              \      /            \      /
 *                               '----'              '----'
 *                         Arcade: Throttle (Y)    Arcade: Sterzo (X)
 *                          Uster: Motore SX (Y)    Uster: Motore DX (Y)
 *
 *
 *
 * -----------------------------------------------------------------------------------------
 *  [!] COMBO ADMIN: Premi [L3 + R3] su GAMEPAD1 o GAMEPAD2 per attivare/disattivare GOD MODE.
 *      Chi preme la combo diventa il "master" e assume il controllo TOTALE (guida, marce,
 *      climbing, flywheel, intake, sparo, servo). L'altro controller resta disattivato.
 *      La Uster Mode NON è disponibile in God Mode, solo in Claudio Mode.
 * =========================================================================================
 */

/*
 * =========================================================================================
 *                              GAMEPAD 2 | MECCANISMI (OPERATOR)
 * =========================================================================================
 *
 *       [L2] ---                                                [R2] ---
 *       [L1] Outtake (spinge anche il flywheel a -500)          [R1] Intake (Aspirazione)
 *
 *              .---| DPAD |---.                                    .---| TASTI |---.
 *             /      .^.       \                                  /      (Y)        \  Y: Toggle Flywheel Full Speed (ON/OFF)
 *            |     <     >      |                                |    (X)   (B)     | B: Toggle Servo (Aperto/Chiuso)
 *             \      'v'       /                                  \      (A)        /  X: Toggle Flywheel Idle (ON/OFF)
 *              '--------------'                                    '---------------'   A: Sparo (Apri Servitori + Intake) *Attivo solo se velocityok*
 *
 *                               ( L3 )              ( R3 )
 *                              /      \            /      \
 *                             | L-STICK|          | R-STICK|
 *                              \      /            \      /
 *                               '----'              '----'
 *                          L3+R3: Attiva God Mode  (Non usato)
 * =========================================================================================
 */


@TeleOp (name = "noccapito quale usare -Zeno",group = "TeleOp Competition")
public class TeleOpMovements_NewControl extends LinearOpMode {
    private final ElapsedTime timerServo = new ElapsedTime();

    // DICHIARARE I MOTORI
    private DcMotor leftMotor, rightMotor, upIntakeMotor, upIntakeSlowMotor, climbMotorInt, climbMotorEst;

    private DcMotorEx flywheel_left, flywheel_right;
    private Servo servitoreRight, servitoreLeft;

    // USE: UPDATE
    private int currentPower = 0;
    private final int maxStep = 16;

    // USE: SERVO TOGGLE (gamepad2.b in Claudio Mode / master.b in God Mode)
    boolean servoToggleOpen = false; // stato del toggle servo (true = aperto, false = chiuso)
    boolean bStateBeforeServo = false; // stato precedente del tasto cerchio per rilevare il click

    // USE: COSTANTI / VARIABILI con valore predefinito
    private static final int TARGET_VELOCITY = 2000;
    private static final int IDLE_VELOCITY = 900;
    private static final double SERVO_CLOSE = 0.01;
    private static final double SERVO_OPEN = 0.17;

    private static final double SERVO_SHOOT = 0.12;
    double scale = 0.75;
    double climbVelocity = 0.5;

    // USE: FLYWHEEL
    boolean flywheelActivate = false;  // toggle Idle (X in Claudio / dpad_left in God Mode)
    boolean yStateBefore = false;
    boolean flywheelFullSpeed = false; // toggle Full Speed (Y in Claudio / Y in God Mode)
    boolean xStateBeforeG2 = false;
    boolean dpadLeftBeforeMaster = false;
    boolean velocityok = false;


    // USE: MOTORS
    double leftPower;
    double rightPower;

    // USE: TOGGLE USTER MODE (SOLO Claudio Mode, gamepad1.b)
    boolean bStateBeforeUster = false;
    boolean usterMode = false;

    // USE: COMBO (attivabile da entrambi i gamepad)
    boolean combo1StateBefore = false;
    boolean combo2StateBefore = false;
    boolean fullController = false;
    boolean masterIsGamepad1 = true; // quale gamepad ha attivato il God Mode

    // USE: MARCE
    boolean lastRightTrigger = false;
    boolean lastLeftTrigger = false;

    // USE: CLIMBING MODE
    boolean xStateBefore = false;
    boolean climbingMode = false;
    boolean lastUpDpad = false;
    boolean lastDownDpad = false;



    @Override
    public void runOpMode() throws InterruptedException{

        /// INITIALIZING SENSORS

        /// INITIALIZING MOTORS
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
        upIntakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
        upIntakeSlowMotor = hardwareMap.get(DcMotor.class, "second_intake_motor");

        servitoreRight = hardwareMap.get(Servo.class, "servitore_1");
        servitoreLeft = hardwareMap.get(Servo.class, "servitore_2");

        flywheel_right = hardwareMap.get(DcMotorEx.class, "flywheel_right");
        flywheel_left = hardwareMap.get(DcMotorEx.class, "flywheel_left");

        climbMotorInt = hardwareMap.get(DcMotor.class, "climb_motor_int");
        climbMotorEst = hardwareMap.get(DcMotor.class, "climb_motor_est");


        /// SET MOVE DIRECTION OF MOTORS
        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);
        upIntakeMotor.setDirection(DcMotor.Direction.FORWARD); // è invertito (-1 intake, +1 outtake)
        upIntakeSlowMotor.setDirection(DcMotor.Direction.REVERSE); // "

        flywheel_right.setDirection(DcMotorEx.Direction.REVERSE);
        flywheel_left.setDirection(DcMotorEx.Direction.REVERSE);

        servitoreRight.setDirection(Servo.Direction.REVERSE);
        servitoreLeft.setDirection(Servo.Direction.FORWARD);

        climbMotorInt.setDirection(DcMotor.Direction.REVERSE);
        climbMotorEst.setDirection(DcMotor.Direction.REVERSE);


        /// RESETTING THE ENCODER
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        climbMotorInt.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        climbMotorEst.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);


        /// SET TO USE THE ENCODER FOR THE SPEED
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        upIntakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        upIntakeSlowMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        climbMotorInt.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        climbMotorEst.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        /// SET THE MOTOR BEHAVIOR WHEN STOPPED
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        upIntakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        upIntakeSlowMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        flywheel_right.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        flywheel_left.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);

        climbMotorInt.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        climbMotorEst.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        /// WHEN THE ROBOT IS READY, PRESS PLAY
        telemetry.addLine("[Initialized] Press Play to start");
        telemetry.update();


        waitForStart();
        while (opModeIsActive()){

            /// -*=======*- COMBO (da entrambi i gamepad) -*=======*-

            boolean combo1Actual = gamepad1.left_stick_button && gamepad1.right_stick_button;
            boolean combo2Actual = gamepad2.left_stick_button && gamepad2.right_stick_button;

            if (combo1Actual && !combo1StateBefore){
                fullController = !fullController;
                if (fullController) masterIsGamepad1 = true;
            }

            if (combo2Actual && !combo2StateBefore){
                fullController = !fullController;
                if (fullController) masterIsGamepad1 = false;
            }

            combo1StateBefore = combo1Actual;
            combo2StateBefore = combo2Actual;

            if (fullController){ /// GOD/ADMIN mode attivata - il gamepad "master" controlla TUTTO

                Gamepad master = masterIsGamepad1 ? gamepad1 : gamepad2;

                // MOVIMENTO (solo Arcade, niente Uster Mode in God Mode)

                double throttle = -master.left_stick_y;
                double spin = master.right_stick_x;
                leftPower = throttle + spin;
                rightPower = throttle - spin;
                double max = Math.max(Math.abs(leftPower),Math.abs(rightPower));
                if (max>1.0){
                    leftPower /= max;
                    rightPower /= max;
                }

                // MARCE

                boolean currentRightTrigger = master.right_trigger > 0.5;
                boolean currentLeftTrigger = master.left_trigger > 0.5;

                if (currentRightTrigger && !lastRightTrigger) {
                    if (scale < 1.0) {
                        scale += 0.25;
                    }
                }

                if (currentLeftTrigger && !lastLeftTrigger) {
                    if (scale > 0.25) {
                        scale -= 0.25;
                    }
                }

                lastRightTrigger = currentRightTrigger;
                lastLeftTrigger = currentLeftTrigger;


                leftPower *= scale;
                rightPower *= scale;

                leftMotor.setPower(leftPower);
                rightMotor.setPower(rightPower);

                // INTAKE, SPARO & TOGGLE SERVO

                boolean bStateActualServo = master.b;

                if (bStateActualServo && !bStateBeforeServo){
                    servoToggleOpen = !servoToggleOpen;
                }
                //aggiunta
                if (!flywheelFullSpeed){
                    velocityok=false;
                }

                if (flywheel_right.getVelocity() > 1700 && flywheel_left.getVelocity() > 1700 && flywheelFullSpeed){
                    velocityok=true;
                }

                bStateBeforeServo = bStateActualServo;

                if (master.a && velocityok) {
                    servitoreRight.setPosition(SERVO_SHOOT);
                    servitoreLeft.setPosition(SERVO_SHOOT);
                    upIntakeMotor.setPower(-0.8);
                    upIntakeSlowMotor.setPower(-0.8);
                }
                else if (master.right_bumper) {
                    servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    upIntakeMotor.setPower(-1);
                    upIntakeSlowMotor.setPower(-1);
                }
                else if (master.left_bumper) {
                    servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    upIntakeMotor.setPower(1);
                    upIntakeSlowMotor.setPower(1);
                    flywheel_left.setVelocity(-500);
                    flywheel_right.setVelocity(-500);
                }
                else {
                    servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    upIntakeMotor.setPower(0);
                    upIntakeSlowMotor.setPower(0);
                }

                // FLYWHEEL - TOGGLE IDLE (dpad_left)

                boolean yStateActual = master.dpad_left;

                if (yStateActual && !yStateBefore){
                    flywheelActivate = !flywheelActivate;
                }

                yStateBefore = yStateActual;

                // FLYWHEEL - TOGGLE FULL SPEED (Y)

                boolean dpadLeftActualMaster = master.y;

                if (dpadLeftActualMaster && !dpadLeftBeforeMaster){
                    flywheelFullSpeed = !flywheelFullSpeed;
                }

                dpadLeftBeforeMaster = dpadLeftActualMaster;

                if (flywheelFullSpeed) {
                    int filteredFullVelocity = update(TARGET_VELOCITY);
                    flywheel_right.setVelocity(filteredFullVelocity);
                    flywheel_left.setVelocity(filteredFullVelocity);

                } else if (flywheelActivate){
                    flywheel_right.setVelocity(IDLE_VELOCITY);
                    flywheel_left.setVelocity(IDLE_VELOCITY);

                } else if (!master.left_bumper){
                    flywheel_right.setVelocity(0);
                    flywheel_left.setVelocity(0);
                }

                // CLIMBING

                boolean xStateActual = master.x;

                if (xStateActual && !xStateBefore){
                    climbingMode = !climbingMode;
                }

                xStateBefore=xStateActual;

                if (climbingMode){
                    climbMotorInt.setPower(climbVelocity);
                    climbMotorEst.setPower(climbVelocity);
                } else {
                    climbMotorInt.setPower(0);
                    climbMotorEst.setPower(0);
                }

                boolean currentUpDpad = master.dpad_up;
                boolean currentDownDpad = master.dpad_down;

                if (currentUpDpad && !lastUpDpad && climbVelocity< 0.9){
                    climbVelocity+=.1;
                }

                if (currentDownDpad && !lastDownDpad && climbVelocity>-0.9){
                    climbVelocity-=.1;
                }

                lastDownDpad = currentDownDpad;
                lastUpDpad = currentUpDpad;


            } else { /// CLAUDIO mode attivata

                /// -*=======*- GAMEPAD1 | GUIDA -*=======*-

                // GUIDA

                boolean bStateActualUster = gamepad1.b;

                if (bStateActualUster && !bStateBeforeUster){
                    usterMode = !usterMode;
                }

                bStateBeforeUster = bStateActualUster;

                if (usterMode){
                    leftPower = -gamepad1.left_stick_y;
                    rightPower = -gamepad1.right_stick_y;

                } else {
                    double throttle = -gamepad1.left_stick_y;
                    double spin = gamepad1.right_stick_x;
                    leftPower = throttle + spin;
                    rightPower = throttle - spin;
                    double max = Math.max(Math.abs(leftPower),Math.abs(rightPower));
                    if (max>1.0){
                        leftPower /= max;
                        rightPower /= max;
                    }
                }

                // MARCE

                boolean currentRightTrigger = gamepad1.right_trigger_pressed;
                boolean currentLeftTrigger = gamepad1.left_trigger_pressed;

                if (currentRightTrigger && !lastRightTrigger) {
                    if (scale < 1.0) {
                        scale += 0.25;
                    }
                }

                if (currentLeftTrigger && !lastLeftTrigger) {
                    if (scale > 0.25) {
                        scale -= 0.25;
                    }
                }

                lastRightTrigger = currentRightTrigger;
                lastLeftTrigger = currentLeftTrigger;

                leftPower *= scale;
                rightPower *= scale;

                leftMotor.setPower(leftPower);
                rightMotor.setPower(rightPower);

                // CLIMBING

                boolean xStateActual = gamepad1.x;

                if (xStateActual && !xStateBefore){
                    climbingMode = !climbingMode;
                }

                xStateBefore=xStateActual;

                if (climbingMode){
                    climbMotorInt.setPower(climbVelocity);
                    climbMotorEst.setPower(climbVelocity);
                } else {
                    climbMotorInt.setPower(0);
                    climbMotorEst.setPower(0);
                }

                boolean currentUpDpad = gamepad1.dpad_up;
                boolean currentDownDpad = gamepad1.dpad_down;

                if (currentUpDpad && !lastUpDpad && climbVelocity< 0.9){
                    climbVelocity+=.1;
                }

                if (currentDownDpad && !lastDownDpad && climbVelocity>-0.9){
                    climbVelocity-=.1;
                }

                lastDownDpad = currentDownDpad;
                lastUpDpad = currentUpDpad;


                /// -*=======*- GAMEPAD2 | MECCANISMI -*=======*-

                // TOGGLE SERVO CON CERCHIO (gamepad2.b) - APERTO/CHIUSO

                boolean bStateActualServo = gamepad2.b;

                if (bStateActualServo && !bStateBeforeServo){
                    servoToggleOpen = !servoToggleOpen;
                }

                //aggiunta - controllo velocityok, stessa logica del God Mode
                if (!flywheelFullSpeed){
                    velocityok=false;
                }

                if (flywheel_right.getVelocity() > 1700 && flywheel_left.getVelocity() > 1700 && flywheelFullSpeed){
                    velocityok=true;
                }

                bStateBeforeServo = bStateActualServo;

                // INTAKE, SPARO & PALLE ALLA FLYWHEEL

                if (gamepad2.a && velocityok) {
                    servitoreRight.setPosition(SERVO_SHOOT);
                    servitoreLeft.setPosition(SERVO_SHOOT);
                    upIntakeMotor.setPower(-0.8);
                    upIntakeSlowMotor.setPower(-0.8);
                }
                else if (gamepad2.right_bumper) {
                    servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    upIntakeMotor.setPower(-1);
                    upIntakeSlowMotor.setPower(0);
                }
                else if (gamepad2.left_bumper) {
                    servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    upIntakeMotor.setPower(1);
                    upIntakeSlowMotor.setPower(1);
                    flywheel_left.setVelocity(-500);
                    flywheel_right.setVelocity(-500);
                }
                else {
                    servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
                    upIntakeMotor.setPower(0);
                    upIntakeSlowMotor.setPower(0);
                }

                // FLYWHEEL - TOGGLE IDLE (X)

                boolean xStateActualG2 = gamepad2.x;

                if (xStateActualG2 && !xStateBeforeG2){
                    flywheelActivate = !flywheelActivate;
                }

                xStateBeforeG2 = xStateActualG2;

                // FLYWHEEL - TOGGLE FULL SPEED (Y)

                boolean yStateActual = gamepad2.y;

                if (yStateActual && !yStateBefore){
                    flywheelFullSpeed = !flywheelFullSpeed;
                }

                yStateBefore = yStateActual;

                if (flywheelFullSpeed) {
                    int filteredFullVelocity = update(TARGET_VELOCITY);
                    flywheel_right.setVelocity(filteredFullVelocity);
                    flywheel_left.setVelocity(filteredFullVelocity);

                } else if (flywheelActivate){
                    flywheel_right.setVelocity(IDLE_VELOCITY);
                    flywheel_left.setVelocity(IDLE_VELOCITY);

                } else if (!gamepad2.left_bumper){
                    flywheel_right.setVelocity(0);
                    flywheel_left.setVelocity(0);
                }
            }




            // A SUMMARY FOR THE DRIVER
            telemetry.addData("Status", "Running");
            telemetry.addData("Left POWER", leftPower);
            telemetry.addData("Right POWER", rightPower);
            telemetry.addData("Left ENCODER", leftMotor.getCurrentPosition());
            telemetry.addData("Right ENCODER", rightMotor.getCurrentPosition());
            telemetry.addData("Intake Power", upIntakeMotor.getPower());
            telemetry.addData("Right Flywheel velocity",flywheel_right.getVelocity());
            telemetry.addData("Left Flywheel velocity", flywheel_left.getVelocity());
            telemetry.addData("Flywheel Idle", (flywheelActivate) ? "ON" : "OFF");
            telemetry.addData("Flywheel Full Speed", (flywheelFullSpeed) ? "ON" : "OFF");
            telemetry.addData("Velocity OK (sparo abilitato)", (velocityok) ? "YES" : "NO");
            telemetry.addData("Servo toggle state", (servoToggleOpen) ? "OPEN" : "CLOSE");
            telemetry.addData("Uster Mode:", (usterMode) ? "Activated" : "You're a louser, press B (solo Claudio Mode)");
            telemetry.addData("Full Control (God Mode)", (fullController) ? ("You are now ADMIN - Master: Gamepad" + (masterIsGamepad1 ? "1" : "2")) : "You are only CLAUDIO");
            telemetry.addData("Speed", (scale==1.0) ? 4 : (scale==0.75) ? 3 : (scale==0.5) ? 2 : (scale==0.25) ? 1 : "Folle");
            telemetry.addData("Climbing mode", (climbingMode) ? "Activated" : "OFF");
            telemetry.addData("Climbing velocity", climbVelocity);
            telemetry.update();
        }
    }

    public int update(int targetPower){

        int error = targetPower - currentPower;
        if (error > maxStep){
            currentPower += maxStep;
        } else if (error < -maxStep){
            currentPower -= maxStep;
        } else{
            currentPower = targetPower;
        }
        return currentPower;
    }
}