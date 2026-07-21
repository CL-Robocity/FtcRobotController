package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp (name = "New Arcade Drive - FGC",group = "TeleOp Competition")
public class TeleOpMovements extends LinearOpMode {
    private DcMotor leftMotor, rightMotor, upIntakeMotor;
    private DcMotorEx flywheel_left, flywheel_right;
    private Servo servitore1, servitore2;
    private double currentPower = 0.0;
    private final double maxStep = 0.04;
    double intakeVelocity;
    private boolean shooting = false;
    private boolean feeding = false;
    private final ElapsedTime feedTimer = new ElapsedTime();
    private static final double TARGET_VELOCITY = 1600.0;
    private static final double SERVO_CLOSE = 0.2;
    private static final double SERVO_OPEN = 1.0;
    private static final int WAIT_TO_PUSH = 1000;

    @Override
    public void runOpMode() throws InterruptedException{

        /// INITIALIZING MOTORS
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
        upIntakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");

        servitore1 = hardwareMap.get(Servo.class, "servitore_1");
        servitore2 = hardwareMap.get(Servo.class, "servitore_2");

        flywheel_right = hardwareMap.get(DcMotorEx.class, "flywheel_right");
        flywheel_left = hardwareMap.get(DcMotorEx.class, "flywheel_left");

        /// SET MOVE DIRECTION OF MOTORS
        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);
        upIntakeMotor.setDirection(DcMotor.Direction.FORWARD);

        flywheel_right.setDirection(DcMotorEx.Direction.FORWARD);
        flywheel_left.setDirection(DcMotorEx.Direction.FORWARD);

        servitore2.setDirection(Servo.Direction.REVERSE);

        /// RESETTING THE ENCODER
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        /// SET USING THE ENCODER FOR THE SPEED
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        upIntakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        /// SET THE MOTOR BEHAVIOR WHEN STOPPED
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        upIntakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        flywheel_right.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        flywheel_left.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);

        /// WHEN THE ROBOT IS READY, PRESS PLAY
        telemetry.addLine("[Initialized] Press Play to start");
        telemetry.update();

        waitForStart();
        while (opModeIsActive()){

            // ARCADE MODE WITH CONTROLLER
            double throttle = -gamepad1.left_stick_y;
            double spin = gamepad1.left_stick_x;

            double leftPower = throttle + spin;
            double rightPower = throttle - spin;

            double max = Math.max(Math.abs(leftPower),Math.abs(rightPower));

            if (max>1.0){
                leftPower /= max;
                rightPower /= max;
            }

            if (gamepad1.right_trigger_pressed){
                leftPower*=0.6;
                rightPower*=0.6;
            }

            // double leftFilteredPower = update(leftPower);
            // double rightFilteredPower = update(rightPower);

            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);

            // INTAKE WITH R1 e R2
            intakeVelocity = (gamepad1.right_bumper) ? 1 : (gamepad1.left_bumper) ? -1 : 0;
            upIntakeMotor.setPower(intakeVelocity);

            // FLYWHEEL WITH TRIANGOLO e QUADRATO
            //
            // in qeusto caso il primo ciclo if else if viene attivato una volta sola quando y o x sono premuti,
            // questo basta e avanza perché .setVelocity() non serve richiamarlo, i motori andranno fino a quando
            // non raggiungono la TRGET_VELOCITY.
            // le altre 2 condizioni if, vengono attivate solamente una di seguito all'altra e solo se il primissimo
            // if si era attivato (shooting = true) e servono per aprire i servo motori e attivare i motori dell'intake.

            if (gamepad1.yWasPressed()){
                shooting = true;
                flywheel_right.setVelocity(TARGET_VELOCITY);
                flywheel_left.setVelocity(TARGET_VELOCITY);
            } else if (gamepad1.xWasPressed()){
                shooting = false;
                feeding = false;
                flywheel_right.setVelocity(0);
                flywheel_left.setVelocity(0);
                servitore1.setPosition(SERVO_CLOSE);  // change the SERVO_CLOSE value at the 23rd line
                servitore2.setPosition(SERVO_CLOSE);
                upIntakeMotor.setPower(0);
            }

            if (shooting && !feeding
                    && flywheel_left.getVelocity()>=(TARGET_VELOCITY-150)
                    && flywheel_right.getVelocity()>=(TARGET_VELOCITY-150)){
                servitore1.setPosition(SERVO_OPEN);  // change the SERVO_OPEN value at the 24th line
                servitore2.setPosition(SERVO_OPEN);
                feeding = true;
                feedTimer.reset();
            }

            if (feeding && feedTimer.milliseconds() > WAIT_TO_PUSH){  // change value at the 25th line
                upIntakeMotor.setPower(1);
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
            telemetry.update();
        }

    }
    public double update(double targetPower){

        if (targetPower==0){
            currentPower=0;
            return currentPower;
        }

        double error = targetPower - currentPower;
        if (error > maxStep){
            error += maxStep;
        } else if (error < -maxStep){
            error -= maxStep;
        } else{
            currentPower = targetPower;
        }
        return currentPower;
    }
}
