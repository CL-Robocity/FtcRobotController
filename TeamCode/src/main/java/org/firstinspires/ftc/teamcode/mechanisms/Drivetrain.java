package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Drivetrain {
    private DcMotor leftMotor, rightMotor;
    private double scale = 0.75;

    public void init(HardwareMap hardwareMap) {
        leftMotor = hardwareMap.get(DcMotor.class, "left_motor");
        rightMotor = hardwareMap.get(DcMotor.class, "right_motor");

        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);

        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void driveArcade(double throttle, double spin) {
        double leftPower = throttle + spin;
        double rightPower = throttle - spin;

        double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
        if (max > 1.0) {
            leftPower /= max;
            rightPower /= max;
        }

        leftMotor.setPower(leftPower * scale);
        rightMotor.setPower(rightPower * scale);
    }

    public void driveTank(double leftPower, double rightPower) {
        leftMotor.setPower(leftPower * scale);
        rightMotor.setPower(rightPower * scale);
    }

    public void gearUp() {
        if (scale < 1.0) scale += 0.25;
    }

    public void gearDown() {
        if (scale > 0.25) scale -= 0.25;
    }

    public double getScale() { return scale; }
    public double getLeftPower() { return leftMotor.getPower(); }
    public double getRightPower() { return rightMotor.getPower(); }
    public int getLeftEncoder() { return leftMotor.getCurrentPosition(); }
    public int getRightEncoder() { return rightMotor.getCurrentPosition(); }
}
