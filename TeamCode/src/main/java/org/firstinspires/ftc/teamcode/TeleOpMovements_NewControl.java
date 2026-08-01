package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp (name = "New Arcade Drive - FGC",group = "TeleOp Competition")
public class TeleOpMovements_NewControl extends LinearOpMode {
    private ElapsedTime timerServo = new ElapsedTime();
    private DcMotor leftMotor, rightMotor, upIntakeMotor, upIntakeSlowMotor;
    private DcMotorEx flywheel_left, flywheel_right;
    private Servo servitoreRight, servitoreLeft;
    private TouchSensor touchSensorLeft, touchSensorRight;
    private CRServo CRServoLeft, CRServoRight;
    private int currentPower = 0;
    private final int maxStep = 16;
    private static final int TARGET_VELOCITY = 2000;
    private static final double SERVO_CLOSE = 0;
    private static final double SERVO_OPEN = 0.12;
    boolean flywheelActivate = false;
    boolean yStateBefore = false;
    boolean servoIsMoving = false;
    boolean barsOut = false;
    boolean barsInside = true;
    double crservoPower = 1.0;
    double leftPower;
    double rightPower;
    boolean bStateBefore = false;
    boolean tankMode = false;

    @Override
    public void runOpMode() throws InterruptedException{

        /// INITIALIZING SENSORS
        touchSensorLeft = hardwareMap.get(TouchSensor.class, "touch_sensor_left");
        touchSensorRight = hardwareMap.get(TouchSensor.class, "touch_sensor_right");

        /// INITIALIZING MOTORS
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
        upIntakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
        upIntakeSlowMotor = hardwareMap.get(DcMotor.class, "second_intake_motor");

        servitoreRight = hardwareMap.get(Servo.class, "servitore_1");
        servitoreLeft = hardwareMap.get(Servo.class, "servitore_2");
        CRServoLeft = hardwareMap.get(CRServo.class, "CRServitore_left");
        CRServoRight = hardwareMap.get(CRServo.class, "CRServitore_right");

        flywheel_right = hardwareMap.get(DcMotorEx.class, "flywheel_right");
        flywheel_left = hardwareMap.get(DcMotorEx.class, "flywheel_left");

        /// SET MOVE DIRECTION OF MOTORS
        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);
        upIntakeMotor.setDirection(DcMotor.Direction.FORWARD); // è invertito (-1 intake, +1 outtake)
        upIntakeSlowMotor.setDirection(DcMotor.Direction.FORWARD); // "

        flywheel_right.setDirection(DcMotorEx.Direction.FORWARD);
        flywheel_left.setDirection(DcMotorEx.Direction.FORWARD);

        servitoreRight.setDirection(Servo.Direction.REVERSE);
        servitoreLeft.setDirection(Servo.Direction.FORWARD);

        CRServoRight.setDirection(CRServo.Direction.REVERSE);
        CRServoLeft.setDirection(CRServo.Direction.FORWARD);

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


            /// -*=======*- GAMEPAD1 | GUIDA -*=======*-

            boolean bStateActual = gamepad1.b;

            if (bStateActual && !bStateBefore){
                tankMode = !tankMode;
            }

            bStateBefore = bStateActual;

            if (tankMode){
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

            double scale = 0.75;

            if (gamepad1.right_trigger_pressed){
                scale = 1.0;
            }
            else if (gamepad1.x){
                scale = 0.5;
            }

            leftPower *= scale;
            rightPower *= scale;

            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);



            /// -*=======*- GAMEPAD2 | MECCANISMI -*=======*-

            // INTAKE WITH R1 e OUTTAKE WITH R2

            if (gamepad2.right_bumper){
                upIntakeMotor.setPower(-1);
            } else if (gamepad2.left_bumper){
                upIntakeMotor.setPower(1);
                upIntakeSlowMotor.setPower(1);
            }

            // FLYWHEEL WITH TRIANGOLO TOGGLE

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

            // OPEN THE SERVOS AND MAKING THE INTAKE GO

            if (gamepad2.a){
                servitoreRight.setPosition(SERVO_OPEN);
                servitoreLeft.setPosition(SERVO_OPEN);
                upIntakeMotor.setPower(-1);
                upIntakeSlowMotor.setPower(-1);
            } else {
                servitoreRight.setPosition(SERVO_CLOSE);
                servitoreLeft.setPosition(SERVO_CLOSE);
                if (!gamepad2.left_bumper && !gamepad2.right_bumper){
                    upIntakeMotor.setPower(0);
                    upIntakeSlowMotor.setPower(0);
                }
            }

            // RETRACT THE EXTENDABLE BARS WITH D_PAD_UP AND D_PAD_DOWN
/*
            if (!servoIsMoving ){
                if (gamepad1.dpad_up && !barsOut){
                    crservoPower = -1.0;
                    timerServo.reset();
                    servoIsMoving = true;
                } else if (gamepad1.dpad_down && !barsInside){
                    crservoPower = 1.0;
                    timerServo.reset();
                    servoIsMoving = true;
                }
            }

            if (servoIsMoving){
                if(timerServo.seconds() <2.0){
                    CRServoLeft.setPower(crservoPower);
                    CRServoRight.setPower(crservoPower);
                } else {
                    CRServoRight.setPower(0);
                    CRServoLeft.setPower(0);
                    if (crservoPower < 0.0){
                        barsOut = true;
                        barsInside = false;
                    } else if (crservoPower > 0.0){
                        barsInside = true;
                        barsOut = false;
                    }
                    servoIsMoving = false;
                }
            } else {
                CRServoLeft.setPower(0);
                CRServoRight.setPower(0);
            }
*/
            // A SUMMARY FOR THE DRIVER
            telemetry.addData("Status", "Running");
            telemetry.addData("Left POWER", leftPower);
            telemetry.addData("Right POWER", rightPower);
            telemetry.addData("Left ENCODER", leftMotor.getCurrentPosition());
            telemetry.addData("Right ENCODER", rightMotor.getCurrentPosition());
            telemetry.addData("Intake Power", upIntakeMotor.getPower());
            telemetry.addData("Right Flywheel velocity",flywheel_right.getVelocity());
            telemetry.addData("Left Flywheel velocity", flywheel_left.getVelocity());
            telemetry.addData("Servo's state", (gamepad2.a) ? "OPEN" : "CLOSE");
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
