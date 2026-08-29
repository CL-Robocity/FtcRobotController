package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.mechanisms.Climber;
import org.firstinspires.ftc.teamcode.mechanisms.Drivetrain;
import org.firstinspires.ftc.teamcode.mechanisms.Flywheel;
import org.firstinspires.ftc.teamcode.mechanisms.Intake;

@TeleOp(name = "TeleOp - FGC Italy", group = "TeleOp Competition")
public class TeleOpDrive_DEF extends LinearOpMode {

    // SOTTOSISTEMI
    private final Drivetrain drivetrain = new Drivetrain();
    private final Flywheel flywheel = new Flywheel();
    private final Intake intake = new Intake();
    private final Climber climber = new Climber();

    // STATI COMBO E MODALITÀ
    private boolean fullController = false;
    private boolean usterMode = false;

    // VARIABILI PER RILEVAMENTO PRESSIONE (RISING EDGE)
    private boolean lastCombo = false;
    private boolean lastRightTrigger = false, lastLeftTrigger = false;
    private boolean lastFlywheelY1 = false, lastFlywheelY2 = false;
    private boolean lastClimbX = false, lastUpDpad = false, lastDownDpad = false;

    @Override
    public void runOpMode() throws InterruptedException {

        // INIZIALIZZAZIONE
        drivetrain.init(hardwareMap);
        flywheel.init(hardwareMap);
        intake.init(hardwareMap);
        climber.init(hardwareMap);

        telemetry.addLine("[Initialized] Press Play to start");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // 1. GESTIONE COMBO (L3 + R3) -> SWITCH GOD MODE / CLAUDIO MODE
            boolean currentCombo = gamepad1.left_stick_button && gamepad1.right_stick_button;
            if (currentCombo && !lastCombo) {
                fullController = !fullController;
            }
            lastCombo = currentCombo;

            // 2. SELEZIONE MODALITÀ DI GUIDA E MECCANISMI
            if (fullController) {
                handleGodMode();
            } else {
                handleClaudioMode();
            }

            // 3. AGGIORNAMENTO SOTTOSISTEMI
            flywheel.update();
            climber.update();

            // 4. TELEMETRIA
            updateTelemetry();
        }
    }

    private void handleGodMode() {
        // GUIDA (Gamepad 1)
        handleDrivetrainControls(gamepad1);
        handleClimberControls(gamepad1, 0.2, 0.9, -0.5);

        // MECCANISMI (Gamepad 1)
        handleIntakeControls(gamepad1, gamepad1.a);
        handleFlywheelToggle(gamepad1.y, true);
    }

    private void handleClaudioMode() {
        // GUIDA (Gamepad 1)
        handleDrivetrainControls(gamepad1);
        handleClimberControls(gamepad1, 0.2, 0.9, -0.5);

        if (gamepad1.b) {
            climber.stopSpeed();
        }

        // MECCANISMI (Gamepad 2)
        handleIntakeControls(gamepad2, gamepad2.a);
        handleFlywheelToggle(gamepad2.y, false);
    }

    private void handleDrivetrainControls(Gamepad gamepad) {
        if (usterMode) {
            drivetrain.driveTank(-gamepad.left_stick_y, -gamepad.right_stick_y);
        } else {
            drivetrain.driveArcade(-gamepad.left_stick_y, gamepad.right_stick_x);
        }

        // Gestione Marce
        boolean rightTrigger = gamepad.right_trigger_pressed;
        boolean leftTrigger = gamepad.left_trigger_pressed;

        if (rightTrigger && !lastRightTrigger) drivetrain.gearUp();
        if (leftTrigger && !lastLeftTrigger) drivetrain.gearDown();

        lastRightTrigger = rightTrigger;
        lastLeftTrigger = leftTrigger;
    }

    private void handleIntakeControls(Gamepad gamepad, boolean shootButton) {
        if (shootButton && flywheel.isReady()) {
            intake.shootFeed();
        } else if (gamepad.right_bumper) {
            intake.intake();
        } else if (gamepad.left_bumper) {
            intake.outtake();
        } else {
            intake.stop();
        }
    }

    private void handleFlywheelToggle(boolean yPressed, boolean isGamepad1) {
        if (isGamepad1) {
            if (yPressed && !lastFlywheelY1) flywheel.toggle();
            lastFlywheelY1 = yPressed;
        } else {
            if (yPressed && !lastFlywheelY2) flywheel.toggle();
            lastFlywheelY2 = yPressed;
        }
    }

    private void handleClimberControls(Gamepad gamepad, double step, double maxSpeed, double minSpeed) {
        boolean xPressed = gamepad.x;
        if (xPressed && !lastClimbX) climber.toggleClimb();
        lastClimbX = xPressed;

        boolean upDpad = gamepad.dpad_up;
        boolean downDpad = gamepad.dpad_down;

        if (upDpad && !lastUpDpad) climber.increaseSpeed(step, maxSpeed);
        if (downDpad && !lastDownDpad) climber.decreaseSpeed(step, minSpeed);

        lastUpDpad = upDpad;
        lastDownDpad = downDpad;
    }

    private void updateTelemetry() {
        telemetry.addData("Status", "Running");
        telemetry.addData("Left POWER", drivetrain.getLeftPower());
        telemetry.addData("Right POWER", drivetrain.getRightPower());
        telemetry.addData("Left ENCODER", drivetrain.getLeftEncoder());
        telemetry.addData("Right ENCODER", drivetrain.getRightEncoder());
        telemetry.addData("Intake Power", intake.getPower());
        telemetry.addData("Flywheel Velocity Left", flywheel.getLeftVelocity());
        telemetry.addData("Flywheel Velocity Right", flywheel.getRightVelocity());
        telemetry.addData("Full Control (God Mode)", fullController ? "ADMIN" : "CLAUDIO");
        telemetry.addData("Speed Scale", drivetrain.getScale());
        telemetry.addLine("APE PIAGGIO 1946-1967");
        telemetry.addData("Climbing Mode", climber.isClimbing() ? "ON" : "OFF");
        telemetry.addData("Climbing Speed", climber.getClimbVelocity());
        telemetry.update();
    }
}

