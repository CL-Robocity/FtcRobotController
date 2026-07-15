package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.opencv.core.Mat;

@TeleOp (name = "Arcade Drive - FGC",group = "TeleOp Competition")
public class TeleOpMovements extends LinearOpMode {
    private DcMotor leftMotor, rightMotor;

    private boolean slowDown;
    private static final double DEADBAND = 0.05;

    @Override
    public void runOpMode(){
        // Inizializzando le variabili dei motori (i nomi sono nei config file)
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");

        // I motori sono specchiati, per andare dritto i motori a sinistra devono andare
        // nell'altro verso
        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);

        // RUN_USING_ENCODER permette di controllare che i motori vadano alla velocità giusta
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // ZeroPowerBehavior.BRAKE ferma forzatamente i motori quando togli potenza
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        telemetry.addLine("[Initialized] Press Play");
        telemetry.update();

        waitForStart();
        while (opModeIsActive()){

            double throttleInput = -gamepad1.left_stick_y; // lo stick da valori invertiti in y
            double spinInput = gamepad1.right_stick_x;

            double throttle = (Math.abs(throttleInput) < DEADBAND) ? 0.0 : throttleInput; // annulla lo stick drift
            double spin = (Math.abs(spinInput) < DEADBAND) ? 0.0 : spinInput;

            throttle = Math.pow(throttle,3); // aumenta la precisione a basse veloctà
            spin = Math.pow(spin,3);

            double leftPower = throttle + spin;
            double rightPower = throttle - spin;

            double max = Math.max(Math.abs(leftPower),Math.abs(rightPower));

            if (max>1.0){ // normalizzare i valori maggiori di 1.0
                leftPower /= max;
                rightPower /= max;
            }


            if (gamepad1.right_trigger_pressed) { slowDown = true;}
            else { slowDown = false; }

            if (slowDown){ // dimezza la velocità massima del robot
                leftPower/=2;
                rightPower/=2;
            }

            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);
        }

    }
}
