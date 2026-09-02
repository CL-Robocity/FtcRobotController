package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Climber {
    private DcMotor climbMotorInt, climbMotorEst;
    private boolean climbingMode = false;
    private double climbVelocity = 0;

    public void init(HardwareMap hardwareMap) {
        climbMotorInt = hardwareMap.get(DcMotor.class, "climb_motor_int");
        climbMotorEst = hardwareMap.get(DcMotor.class, "climb_motor_est");

        climbMotorInt.setDirection(DcMotor.Direction.REVERSE);
        climbMotorEst.setDirection(DcMotor.Direction.REVERSE);

        climbMotorInt.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        climbMotorEst.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        climbMotorInt.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        climbMotorEst.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        climbMotorInt.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        climbMotorEst.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    public void toggleClimb() {
        climbingMode = !climbingMode;
    }

    public void increaseSpeed(double step, double max) {
        if (climbVelocity < max) climbVelocity += step;
    }

    public void decreaseSpeed(double step, double min) {
        if (climbVelocity > min) climbVelocity -= step;
    }

    public void stopSpeed() {
        climbVelocity = 0;
    }

    public void update() {
        if (climbingMode) {
            climbMotorInt.setPower(climbVelocity);
            climbMotorEst.setPower(climbVelocity);
        } else {
            climbMotorInt.setPower(0);
            climbMotorEst.setPower(0);
        }
    }

    public boolean isClimbing() { return climbingMode; }
    public double getClimbVelocity() { return climbVelocity; }
}
