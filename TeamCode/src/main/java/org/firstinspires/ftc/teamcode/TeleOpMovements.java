package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp (name = "New Arcade Drive - FGC",group = "TeleOp Competition")
public class TeleOpMovements extends LinearOpMode {
    private DcMotor leftMotor, rightMotor, upIntakeMotor, upIntakeSlowMotor;
    private DcMotorEx flywheel_left, flywheel_right;
    private Servo servitore_right, servitore_left;
    private int currentPower = 0;
    private final int maxStep = 16;
    private static final int TARGET_VELOCITY = 1600;
    private static final double SERVO_CLOSE = 0;
    private static final double SERVO_OPEN = 0.15;
    boolean flywheelActivate = false;
    boolean yStateBefore = false;

    @Override
    public void runOpMode() throws InterruptedException{

        /// INITIALIZING MOTORS
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
        upIntakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
        upIntakeSlowMotor = hardwareMap.get(DcMotor.class, "second_intake_motor");

        servitore_right = hardwareMap.get(Servo.class, "servitore_1");
        servitore_left = hardwareMap.get(Servo.class, "servitore_2");

        flywheel_right = hardwareMap.get(DcMotorEx.class, "flywheel_right");
        flywheel_left = hardwareMap.get(DcMotorEx.class, "flywheel_left");

        /// SET MOVE DIRECTION OF MOTORS
        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);
        upIntakeMotor.setDirection(DcMotor.Direction.FORWARD); // è invertito (-1 intake, +1 outtake)
        upIntakeSlowMotor.setDirection(DcMotor.Direction.FORWARD); // "

        flywheel_right.setDirection(DcMotorEx.Direction.FORWARD);
        flywheel_left.setDirection(DcMotorEx.Direction.FORWARD);

        servitore_right.setDirection(Servo.Direction.REVERSE);

        /// RESETTING THE ENCODER
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        /// SET TO USE THE ENCODER FOR THE SPEED
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        upIntakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        upIntakeSlowMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        /// SET THE MOTOR BEHAVIOR WHEN STOPPED
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        upIntakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        upIntakeSlowMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        flywheel_right.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        flywheel_left.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);

        /// WHEN THE ROBOT IS READY, PRESS PLAY
        telemetry.addLine("[Initialized] Press Play to start");
        telemetry.update();

        waitForStart();
        while (opModeIsActive()){

            // ARCADE MODE WITH CONTROLLER (LEFT STICK GO BACK AND FORWARD, RIGHT STICK GO LEFT AND RIGHT
            double throttle = -gamepad1.left_stick_y;
            double spin = gamepad1.right_stick_x;

            double leftPower = throttle + spin;
            double rightPower = throttle - spin;

            double max = Math.max(Math.abs(leftPower),Math.abs(rightPower));

            if (max>1.0){
                leftPower /= max;
                rightPower /= max;
            }

            // PRESS R2 FOR MAKING THE ROBOT GO SLOWER
            if (gamepad1.right_trigger_pressed){
                leftPower*=0.6;
                rightPower*=0.6;
            }

            // PRESS R2 FOR MAKING THE ROBOT GO SLOWER
            if (gamepad1.left_trigger_pressed){
                leftPower*=0.2;
                rightPower*=0.2;
            }

            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);

            // INTAKE WITH R1 e R2

            if (gamepad1.right_bumper){
                upIntakeMotor.setPower(-1);
            } else if (gamepad1.left_bumper){
                upIntakeMotor.setPower(1);
                upIntakeSlowMotor.setPower(1);
            }

            // FLYWHEEL WITH TRIANGOLO TOGGLE

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

            // OPEN THE SERVOS AND MAKING THE INTAKE GO

            if (gamepad1.a){
                servitore_right.setPosition(SERVO_OPEN);
                servitore_left.setPosition(SERVO_OPEN);
                upIntakeMotor.setPower(-1);
                upIntakeSlowMotor.setPower(-1);
            } else {
                servitore_right.setPosition(SERVO_CLOSE);
                servitore_left.setPosition(SERVO_CLOSE);
                if (!gamepad1.left_bumper && !gamepad1.right_bumper){
                    upIntakeMotor.setPower(0);
                    upIntakeSlowMotor.setPower(0);
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
            telemetry.addData("Servo's state", (gamepad1.a) ? "OPEN" : "CLOSE");
            telemetry.update();
        }
    }

    // STA ROBA NON FUNZIONA, È DA GUARDARE
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
