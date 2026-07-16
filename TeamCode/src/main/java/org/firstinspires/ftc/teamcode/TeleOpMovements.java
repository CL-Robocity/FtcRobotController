package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.opencv.core.Mat;

@TeleOp (name = "New Arcade Drive - FGC",group = "TeleOp Competition")
public class TeleOpMovements extends LinearOpMode {
    private DcMotor leftMotor, rightMotor, upIntakeMotor;
    private DcMotorEx flywheel;

    private boolean slowDown;
    double intakeVelocity;
    private static final double DEADBAND = 0.05;
    private static final double IDLE_VELOCITY = 1000.0;
    private static final double TARGET_VELOCITY = 2200.0;

    @Override
    public void runOpMode(){
        // Inizializzando le variabili dei motori (i nomi sono nei config file)
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");
        upIntakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
        flywheel = hardwareMap.get(DcMotorEx.class, "outtake_motor");

        // I motori sono specchiati, per andare dritto i motori a sinistra devono andare
        // nell'altro verso
        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);
        upIntakeMotor.setDirection(DcMotor.Direction.FORWARD);
        flywheel.setDirection(DcMotorSimple.Direction.FORWARD);

        // Resetta la posizione dell'encoder all'avvio
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        flywheel.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // RUN_USING_ENCODER permette di controllare che i motori vadano alla velocità giusta
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        upIntakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        flywheel.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        // ZeroPowerBehavior.BRAKE ferma forzatamente i motori quando togli potenza
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        upIntakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        flywheel.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);


        telemetry.addLine("[Initialized] Press Play to start");
        telemetry.update();

        waitForStart();
        while (opModeIsActive()){

            double throttleInput = -gamepad1.left_stick_y; // lo stick da valori invertiti in y
            double spinInput = gamepad1.left_stick_x;

            double throttle = (Math.abs(throttleInput) < DEADBAND) ? 0.0 : throttleInput; // annulla lo stick drift
            double spin = (Math.abs(spinInput) < DEADBAND) ? 0.0 : spinInput;
            /*
            throttle = Math.pow(throttle,2); // aumenta la precisione a basse veloctà
            spin = Math.pow(spin,3);
            */
            double leftPower = throttle + spin;
            double rightPower = throttle - spin;

            double max = Math.max(Math.abs(leftPower),Math.abs(rightPower));

            if (max>1.0){ // normalizzare i valori maggiori di 1.0
                leftPower /= max;
                rightPower /= max;
            }

            intakeVelocity = (gamepad1.left_bumper) ? 1 : (gamepad1.right_bumper) ? -1 : 0;
            upIntakeMotor.setPower(intakeVelocity);

            if (gamepad1.cross) {
                flywheel.setVelocity(TARGET_VELOCITY);
                    // meccanismo per spingere le balls verso la flywheel
                    // if (flywheel.getVelocity()>=TARGET_VELOCITY){
                    // }

            } else {
                flywheel.setVelocity(IDLE_VELOCITY);
            }


            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);

            telemetry.addData("Status", "Running");
            telemetry.addData("Drive mode", slowDown ? "SLOW 50%" : "NORMAL 100%");
            telemetry.addData("Left POWER", leftPower);
            telemetry.addData("Right POWER", rightPower);
            telemetry.addData("Left ENCODER", leftMotor.getCurrentPosition());
            telemetry.addData("Right ENCODER", rightMotor.getCurrentPosition());
            telemetry.addData("Intake Power", upIntakeMotor.getPower());
            telemetry.addData("Flywheel velocity",flywheel.getVelocity());
            telemetry.update();
        }

    }
}
