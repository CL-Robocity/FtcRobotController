package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Flywheel {
    private DcMotorEx flywheelLeft, flywheelRight;
    private static final int TARGET_VELOCITY = 1800;
    private static final int MAX_STEP = 16;
    private int currentVelocity = 0;
    private boolean active = false;

    public void init(HardwareMap hardwareMap) {
        flywheelLeft = hardwareMap.get(DcMotorEx.class, "flywheel_left");
        flywheelRight = hardwareMap.get(DcMotorEx.class, "flywheel_right");

        flywheelLeft.setDirection(DcMotorEx.Direction.REVERSE);
        flywheelRight.setDirection(DcMotorEx.Direction.REVERSE);

        flywheelLeft.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        flywheelRight.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        flywheelLeft.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        flywheelRight.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        flywheelLeft.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        flywheelRight.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
    }

    public void toggle() {
        active = !active;
    }

    public void update() {
        if (active) {
            int error = TARGET_VELOCITY - currentVelocity;
            if (error > MAX_STEP) currentVelocity += MAX_STEP;
            else if (error < -MAX_STEP) currentVelocity -= MAX_STEP;
            else currentVelocity = TARGET_VELOCITY;

            flywheelLeft.setVelocity(currentVelocity);
            flywheelRight.setVelocity(currentVelocity);
        } else {
            currentVelocity = 0;
            flywheelLeft.setVelocity(0);
            flywheelRight.setVelocity(0);
        }
    }

    public boolean isReady() {
        return flywheelLeft.getVelocity() > TARGET_VELOCITY;
    }

    public boolean isActive() { return active; }
    public double getLeftVelocity() { return flywheelLeft.getVelocity(); }
    public double getRightVelocity() { return flywheelRight.getVelocity(); }
}
