package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * =========================================================================================
 *                                GAMEPAD 1 | GUIDA (DRIVER)
 * =========================================================================================
 *
 *       [L2] Marcia GIÙ (-25%)                                  [R2] Marcia SÙ (+25%)
 *       [L1] [God Mode] Outtake                                 [R1] [God Mode] Intake (Aspirazione)
 *
 *              .---| DPAD |---.                                    .---| TASTI |---.
 *             /      .^.       \                                  /      (Y)        \  Y: [God Mode] Flywheel ON/OFF
 *            |     <     >      |                                |    (X)   (B)     | B: Toggle Uster / Arcade
 *             \      'v'       /                                  \      (A)        /  X: Toggle Climbing
 *              '--------------'                                    '---------------'   A: [God Mode] Sparo Flywheel
 *             ^: Climb Speed +0.1
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
 *  [!] COMBO ADMIN: Premi [L3 + R3] insieme per alternare CLAUDIO MODE <-> GOD MODE
 * =========================================================================================
 */

/*
 * =========================================================================================
 *                              GAMEPAD 2 | MECCANISMI (OPERATOR)
 * =========================================================================================
 *
 *       [L2] ---                                                [R2] ---
 *       [L1] Outtake                                            [R1] Intake (Aspirazione)
 *
 *              .---| DPAD |---.                                    .---| TASTI |---.
 *             /      .^.       \                                  /      (Y)        \  Y: Toggle Flywheel (ON/OFF)
 *            |     <     >      |                                |    (X)   (B)     | B: ---
 *             \      'v'       /                                  \      (A)        /  X: ---
 *              '--------------'                                    '---------------'   A: Sparo (Apri Servitori + Intake)
 *                                                                                        *Attivo solo se RPM > 1800*
 *
 *                               ( L3 )              ( R3 )
 *                              /      \            /      \
 *                             | L-STICK|          | R-STICK|
 *                              \      /            \      /
 *                               '----'              '----'
 *                             (Non usato)         (Non usato)
 * =========================================================================================
 */


@TeleOp (name = "New Controller - FGC",group = "TeleOp Competition")
public class TeleOpMovements_NewControl extends LinearOpMode {
    private ElapsedTime timerServo = new ElapsedTime();
    private DcMotor leftMotor, rightMotor, upIntakeMotor, upIntakeSlowMotor, climbMotor;
    private DcMotorEx flywheel_left, flywheel_right;
    private Servo servitoreRight, servitoreLeft;

    private int currentPower = 0;
    private final int maxStep = 16;
    private static final int TARGET_VELOCITY = 2000;
    private static final double SERVO_CLOSE = 0.04;
    private static final double SERVO_OPEN = 0.12;
    boolean flywheelActivate = false;
    boolean yStateBefore = false;
    double leftPower;
    double rightPower;
    boolean bStateBefore = false;
    boolean usterMode = false;
    boolean comboStateBefore = false;
    boolean fullController = false;
    boolean comboStateActual = false;
    double scale = 0.75;
    boolean lastRightTrigger = false;
    boolean lastLeftTrigger = false;
    boolean xStateBefore = false;
    boolean climbingMode = false;
    double climbVelocity = 0.5;

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

        climbMotor = hardwareMap.get(DcMotor.class, "climb_motor");

        /// SET MOVE DIRECTION OF MOTORS
        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);
        upIntakeMotor.setDirection(DcMotor.Direction.FORWARD); // è invertito (-1 intake, +1 outtake)
        upIntakeSlowMotor.setDirection(DcMotor.Direction.REVERSE); // "

        flywheel_right.setDirection(DcMotorEx.Direction.REVERSE);
        flywheel_left.setDirection(DcMotorEx.Direction.REVERSE);

        servitoreRight.setDirection(Servo.Direction.REVERSE);
        servitoreLeft.setDirection(Servo.Direction.FORWARD);

        climbMotor.setDirection(DcMotor.Direction.REVERSE);

        /// RESETTING THE ENCODER
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        climbMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        /// SET TO USE THE ENCODER FOR THE SPEED
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        upIntakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        upIntakeSlowMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        climbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        /// SET THE MOTOR BEHAVIOR WHEN STOPPED
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        upIntakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        upIntakeSlowMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        flywheel_right.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        flywheel_left.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);

        climbMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        /// WHEN THE ROBOT IS READY, PRESS PLAY
        telemetry.addLine("[Initialized] Press Play to start");
        telemetry.update();


        waitForStart();
        while (opModeIsActive()){

            /// COMBO

            if (gamepad1.left_stick_button && gamepad1.right_stick_button){
                comboStateActual = true;
            } else {
                comboStateActual = false;

            }

            if (comboStateActual && !comboStateBefore){
                fullController = !fullController;
            }

            comboStateBefore = comboStateActual;

            if (fullController){ /// GOD/ADMIN mode acctivated

                // MOVIMENTO

                boolean bStateActual = gamepad1.b;

                if (bStateActual && !bStateBefore){
                    usterMode = !usterMode;
                }

                bStateBefore = bStateActual;

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

                boolean currentRightTrigger = gamepad1.right_trigger_pressed;
                boolean currentLeftTrigger = gamepad1.left_trigger_pressed;

                // MARCE

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

                // INTAKE & PALLE ALLA FLYWHEEL

                if (gamepad1.a && flywheel_left.getVelocity() > 1800 ) {
                    servitoreRight.setPosition(SERVO_OPEN);
                    servitoreLeft.setPosition(SERVO_OPEN);
                    upIntakeMotor.setPower(-1);
                    upIntakeSlowMotor.setPower(-1);
                }

                else if (gamepad1.right_bumper) {
                    servitoreRight.setPosition(SERVO_CLOSE);
                    servitoreLeft.setPosition(SERVO_CLOSE);
                    upIntakeMotor.setPower(-1);
                    upIntakeSlowMotor.setPower(0);
                }
                else if (gamepad1.left_bumper) {
                    servitoreRight.setPosition(SERVO_CLOSE);
                    servitoreLeft.setPosition(SERVO_CLOSE);
                    upIntakeMotor.setPower(1);
                    upIntakeSlowMotor.setPower(1);
                }
                else {
                    servitoreRight.setPosition(SERVO_CLOSE);
                    servitoreLeft.setPosition(SERVO_CLOSE);
                    upIntakeMotor.setPower(0);
                    upIntakeSlowMotor.setPower(0);
                }

                // FLYWHEEL

                boolean yStateActual = gamepad1.y;

                if (yStateActual && !yStateBefore){
                    flywheelActivate = !flywheelActivate;
                }

                yStateBefore = yStateActual;

                if (flywheelActivate){
                    int filteredTargetVelocity = update(TARGET_VELOCITY);
                    flywheel_right.setVelocity(filteredTargetVelocity);
                    flywheel_left.setVelocity(filteredTargetVelocity);

                } else {
                    flywheel_right.setVelocity(0);
                    flywheel_left.setVelocity(0);
                }

                // CLIMBING

                boolean xStateActual = gamepad1.x;

                if (xStateActual && !xStateBefore){
                    climbingMode = !climbingMode;
                }

                xStateBefore=xStateActual;

                if (climbingMode){
                    climbMotor.setPower(climbVelocity);
                } else {
                    climbMotor.setPower(0);
                }

                if (gamepad1.dpad_up && climbVelocity< 1.0){
                    climbVelocity+=.1;
                } else if (gamepad1.dpad_down&&climbVelocity>0.1){
                    climbVelocity-=.1;
                }

            } else { /// CLAUDIO mode attivata

                /// -*=======*- GAMEPAD1 | GUIDA -*=======*-

                // GUIDA

                boolean bStateActual = gamepad1.b;

                if (bStateActual && !bStateBefore){
                    usterMode = !usterMode;
                }

                bStateBefore = bStateActual;

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
                    climbMotor.setPower(climbVelocity);
                } else {
                    climbMotor.setPower(0);
                }

                if (gamepad1.dpad_up && climbVelocity< 1.0){
                    climbVelocity+=.1;
                } else if (gamepad1.dpad_down&&climbVelocity>0.1){
                    climbVelocity-=.1;
                }

                /// -*=======*- GAMEPAD2 | MECCANISMI -*=======*-

                // INTAKE & PALLE ALLA FLYWHEEL

                if (gamepad2.a && flywheel_left.getVelocity() > 1800) {
                    servitoreRight.setPosition(SERVO_OPEN);
                    servitoreLeft.setPosition(SERVO_OPEN);
                    upIntakeMotor.setPower(-1);
                    upIntakeSlowMotor.setPower(-1);
                }
                else if (gamepad2.right_bumper) {
                    servitoreRight.setPosition(SERVO_CLOSE);
                    servitoreLeft.setPosition(SERVO_CLOSE);
                    upIntakeMotor.setPower(-1);
                    upIntakeSlowMotor.setPower(0);
                }
                else if (gamepad2.left_bumper) {
                    servitoreRight.setPosition(SERVO_CLOSE);
                    servitoreLeft.setPosition(SERVO_CLOSE);
                    upIntakeMotor.setPower(1);
                    upIntakeSlowMotor.setPower(1);
                }
                else {
                    servitoreRight.setPosition(SERVO_CLOSE);
                    servitoreLeft.setPosition(SERVO_CLOSE);
                    upIntakeMotor.setPower(0);
                    upIntakeSlowMotor.setPower(0);
                }

                // FLYWHEEL

                boolean yStateActual = gamepad2.y;

                if (yStateActual && !yStateBefore){
                    flywheelActivate = !flywheelActivate;
                }

                yStateBefore = yStateActual;

                if (flywheelActivate){
                    int filteredTargetVelocity = update(TARGET_VELOCITY);
                    flywheel_right.setVelocity(filteredTargetVelocity);
                    flywheel_left.setVelocity(filteredTargetVelocity);

                } else {
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
            telemetry.addData("Uster Mode:", (usterMode) ? "Activated" : "You're a louser, press O");
            telemetry.addData("Full Control (God Mode)", (fullController) ? "You are now ADMIN" : "You are only CLAUDIO");
            telemetry.addData("Speed", (scale==1.0) ? 4 : (scale==0.75) ? 3 : (scale==0.5) ? 2 : (scale==0.25) ? 1 : "Folle");
            telemetry.addData("Climbing mode", (climbingMode) ? "Activated" : "OFF");
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
