/*programma caricato e aggiornato 3/9/26 11:29
SOFTWARE
aggiunta controllo indipendente open servo,
aggiunta rotazione slowintake motor in intake,
aggiunta rotazione inversa flywheel in outtake,
modoifica utilizzo idle velocity (c'è ancora ma non serve)

HARDWARE
modifica altezza primo rullo intake
aggiunta due pezzi per blocco incastro palline sia buco sopra centrale che buchi laterali flywheel
prima prova slider
 */

/*programma caricato e aggiornato 4/9/26
SOFTWARE
aggiunta controllo unico prima dello sparo a 1700 giri
implementazione di tutte le funzioni sia su godmode che su claudiomode

PROGRAMMMA REFACTORIZZATO
La logica di meccanismi, climbing e guida era duplicata quasi identica tra Claudio Mode
e God Mode: due copie della stessa cosa che dovevano essere tenute manualmente allineate
(fonte di bug quando si modificava una copia e ci si dimenticava dell'altra).
Ora la logica vive in metodi unici (handleMechanisms, handleClimbing, driveArcade,
applyGearAndDrive), chiamati sia da Claudio Mode che da God Mode passando i tasti giusti
per ciascuna modalità. Il comportamento a runtime non cambia, cambia solo come è organizzato
il codice. Vedi i commenti nella sezione "METODI CONDIVISI" più sotto per i dettagli.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/*
 * =========================================================================================
 *                                GAMEPAD 1 | GUIDA (DRIVER)
 * =========================================================================================
 *
 *       [L2] Marcia GIÙ (-25%)                                  [R2] Marcia SÙ (+25%)
 *       [L1] Outtake                                            [R1] Intake (Aspirazione)
 *
 *              .---| DPAD |---.                                    .---| TASTI |---.
 *             /      .^.       \                                  /      (Y)        \  Y: [God Mode] Flywheel Full Speed ON/OFF
 *            |     <     >      |                                |    (X)   (B)     | B: Toggle Uster / Arcade (SOLO Claudio Mode)
 *             \      'v'       /                                  \      (A)        /  X: Toggle Climbing
 *              '--------------'                                    '---------------'   A: [God Mode] Sparo Flywheel (se velocityok)
 *             ^: Climb Speed +0.1        [God Mode] dpad_left: Flywheel Idle ON/OFF
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
 *  [!] COMBO ADMIN: Premi [L3 + R3] su GAMEPAD1 o GAMEPAD2 per attivare/disattivare GOD MODE.
 *      Chi preme la combo diventa il "master" e assume il controllo TOTALE (guida, marce,
 *      climbing, flywheel, intake, sparo, servo). L'altro controller resta disattivato.
 *      La Uster Mode NON è disponibile in God Mode, solo in Claudio Mode.
 * =========================================================================================
 */

/*
 * =========================================================================================
 *                              GAMEPAD 2 | MECCANISMI (OPERATOR)
 * =========================================================================================
 *
 *       [L2] ---                                                [R2] ---
 *       [L1] Outtake (spinge anche il flywheel a -500)          [R1] Intake (Aspirazione)
 *
 *              .---| DPAD |---.                                    .---| TASTI |---.
 *             /      .^.       \                                  /      (Y)        \  Y: Toggle Flywheel Full Speed (ON/OFF)
 *            |     <     >      |                                |    (X)   (B)     | B: Toggle Servo (Aperto/Chiuso)
 *             \      'v'       /                                  \      (A)        /  X: Toggle Flywheel Idle (ON/OFF)
 *              '--------------'                                    '---------------'   A: Sparo (Apri Servitori + Intake) *Attivo solo se velocityok*
 *
 *                               ( L3 )              ( R3 )
 *                              /      \            /      \
 *                             | L-STICK|          | R-STICK|
 *                              \      /            \      /
 *                               '----'              '----'
 *                          L3+R3: Attiva God Mode  (Non usato)
 * =========================================================================================
 */


@TeleOp (name = "testmodificheuniche",group = "TeleOp Competition")
public class TeleOpMovements_NewControl extends LinearOpMode {
    private final ElapsedTime timerServo = new ElapsedTime();

    // DICHIARARE I MOTORI
    private DcMotor leftMotor, rightMotor, upIntakeMotor, upIntakeSlowMotor, climbMotorInt, climbMotorEst;

    private DcMotorEx flywheel_left, flywheel_right;
    private Servo servitoreRight, servitoreLeft;

    // USE: UPDATE (rampa di velocità per il flywheel, vedi metodo update() in fondo al file)
    private int currentPower = 0;
    private final int maxStep = 16;

    // USE: SERVO TOGGLE (tasto B: gamepad2 in Claudio Mode / master in God Mode)
    boolean servoToggleOpen = false; // stato del toggle servo (true = aperto, false = chiuso)
    boolean bStateBeforeServo = false; // stato precedente del tasto per rilevare il click (edge detection)

    // USE: COSTANTI / VARIABILI con valore predefinito
    private static final int TARGET_VELOCITY = 2000;
    private static final int IDLE_VELOCITY = 900;
    private static final double SERVO_CLOSE = 0.01;
    private static final double SERVO_OPEN = 0.17;

    private static final double SERVO_SHOOT = 0.12;
    double scale = 0.75;
    double climbVelocity = 0.5;

    // USE: FLYWHEEL
    boolean flywheelActivate = false;  // toggle Idle (X in Claudio / dpad_left in God Mode)
    boolean yStateBefore = false;      // edge detection per il toggle Full Speed (condiviso tra le due modalità)
    boolean flywheelFullSpeed = false; // toggle Full Speed (Y in entrambe le modalità)
    boolean xStateBeforeG2 = false;    // edge detection per il toggle Idle (condiviso tra le due modalità)
    boolean velocityok = false;        // true quando entrambi i flywheel sono a regime (>1700) con Full Speed attivo


    // USE: MOTORS
    double leftPower;
    double rightPower;

    // USE: TOGGLE USTER MODE (SOLO Claudio Mode, gamepad1.b)
    boolean bStateBeforeUster = false;
    boolean usterMode = false;

    // USE: COMBO (attivabile da entrambi i gamepad)
    boolean combo1StateBefore = false;
    boolean combo2StateBefore = false;
    boolean fullController = false;
    boolean masterIsGamepad1 = true; // quale gamepad ha attivato il God Mode

    // USE: MARCE
    boolean lastRightTrigger = false;
    boolean lastLeftTrigger = false;

    // USE: CLIMBING MODE
    boolean xStateBefore = false;
    boolean climbingMode = false;
    boolean lastUpDpad = false;
    boolean lastDownDpad = false;



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

        climbMotorInt = hardwareMap.get(DcMotor.class, "climb_motor_int");
        climbMotorEst = hardwareMap.get(DcMotor.class, "climb_motor_est");


        /// SET MOVE DIRECTION OF MOTORS
        leftMotor.setDirection(DcMotor.Direction.REVERSE);
        rightMotor.setDirection(DcMotor.Direction.FORWARD);
        upIntakeMotor.setDirection(DcMotor.Direction.FORWARD); // è invertito (-1 intake, +1 outtake)
        upIntakeSlowMotor.setDirection(DcMotor.Direction.REVERSE); // "

        flywheel_right.setDirection(DcMotorEx.Direction.REVERSE);
        flywheel_left.setDirection(DcMotorEx.Direction.REVERSE);

        servitoreRight.setDirection(Servo.Direction.REVERSE);
        servitoreLeft.setDirection(Servo.Direction.FORWARD);

        climbMotorInt.setDirection(DcMotor.Direction.REVERSE);
        climbMotorEst.setDirection(DcMotor.Direction.REVERSE);


        /// RESETTING THE ENCODER
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);

        climbMotorInt.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        climbMotorEst.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);


        /// SET TO USE THE ENCODER FOR THE SPEED
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        upIntakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        upIntakeSlowMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        flywheel_right.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        flywheel_left.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        climbMotorInt.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        climbMotorEst.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        /// SET THE MOTOR BEHAVIOR WHEN STOPPED
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        upIntakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        upIntakeSlowMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        flywheel_right.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        flywheel_left.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);

        climbMotorInt.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        climbMotorEst.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        /// WHEN THE ROBOT IS READY, PRESS PLAY
        telemetry.addLine("[Initialized] Press Play to start");
        telemetry.update();


        waitForStart();
        while (opModeIsActive()){

            /// -*=======*- COMBO (da entrambi i gamepad) -*=======*-
            // L3+R3 su un gamepad attiva il God Mode e lo rende "master".
            // L3+R3 su un gamepad (uno qualsiasi) mentre il God Mode è attivo lo disattiva.

            boolean combo1Actual = gamepad1.left_stick_button && gamepad1.right_stick_button;
            boolean combo2Actual = gamepad2.left_stick_button && gamepad2.right_stick_button;

            if (combo1Actual && !combo1StateBefore){
                fullController = !fullController;
                if (fullController) masterIsGamepad1 = true;
            }

            if (combo2Actual && !combo2StateBefore){
                fullController = !fullController;
                if (fullController) masterIsGamepad1 = false;
            }

            combo1StateBefore = combo1Actual;
            combo2StateBefore = combo2Actual;

            // -*=======*- SCELTA DEL GAMEPAD ATTIVO -*=======*-
            // "driver" è il gamepad che guida il robot in questo ciclo:
            //   - in God Mode è il master (unico gamepad che comanda tutto)
            //   - in Claudio Mode è sempre gamepad1
            // "operator" è il gamepad che comanda i meccanismi in questo ciclo:
            //   - in God Mode è di nuovo il master (stesso gamepad fa tutto)
            //   - in Claudio Mode è sempre gamepad2
            // Grazie a questa astrazione, handleMechanisms() e handleClimbing() vengono
            // chiamati UNA SOLA VOLTA a riga di codice, sia che si sia in God Mode che in
            // Claudio Mode: cambia solo quale gamepad fisico viene passato come parametro.

            Gamepad master = masterIsGamepad1 ? gamepad1 : gamepad2;
            Gamepad driver = fullController ? master : gamepad1;
            Gamepad operator = fullController ? master : gamepad2;

            // -*=======*- GUIDA -*=======*-
            // La Uster Mode (doppio stick stile tank) esiste SOLO in Claudio Mode.
            // In God Mode si guida sempre in Arcade con il gamepad master.

            if (!fullController){
                boolean bStateActualUster = gamepad1.b;
                if (bStateActualUster && !bStateBeforeUster){
                    usterMode = !usterMode;
                }
                bStateBeforeUster = bStateActualUster;
            }

            if (!fullController && usterMode){
                leftPower = -driver.left_stick_y;
                rightPower = -driver.right_stick_y;
            } else {
                driveArcade(-driver.left_stick_y, driver.right_stick_x);
            }

            applyGearAndDrive(driver);

            // -*=======*- CLIMBING -*=======*-
            // Stessa logica e stessi tasti (X, dpad su/giù) sia in Claudio che in God Mode:
            // basta passare il gamepad giusto.

            handleClimbing(driver);

            // -*=======*- MECCANISMI (intake, sparo, servo, flywheel) -*=======*-
            // Stessa logica in entrambe le modalità. Cambiano solo i TASTI usati per
            // Idle/Full Speed, perché in God Mode il tasto X è già occupato dal Climbing
            // (sullo stesso gamepad master), quindi l'Idle viene spostato su dpad_left.
            // In Claudio Mode invece climbing e meccanismi sono su due gamepad diversi,
            // quindi non c'è conflitto e l'Idle resta su X.

            if (fullController){
                handleMechanisms(
                        operator.a,            // sparo
                        operator.b,             // toggle servo
                        operator.dpad_left,     // toggle Idle (spostato per non collidere con Climbing su X)
                        operator.y,             // toggle Full Speed
                        operator.right_bumper,  // intake
                        operator.left_bumper    // outtake
                );
            } else {
                handleMechanisms(
                        operator.a,            // sparo
                        operator.b,             // toggle servo
                        operator.x,             // toggle Idle
                        operator.y,             // toggle Full Speed
                        operator.right_bumper,  // intake
                        operator.left_bumper    // outtake
                );
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
            telemetry.addData("Flywheel Idle", (flywheelActivate) ? "ON" : "OFF");
            telemetry.addData("Flywheel Full Speed", (flywheelFullSpeed) ? "ON" : "OFF");
            telemetry.addData("Velocity OK (sparo abilitato)", (velocityok) ? "YES" : "NO");
            telemetry.addData("Servo toggle state", (servoToggleOpen) ? "OPEN" : "CLOSE");
            telemetry.addData("Uster Mode:", (usterMode) ? "Activated" : "You're a louser, press B (solo Claudio Mode)");
            telemetry.addData("Full Control (God Mode)", (fullController) ? ("You are now ADMIN - Master: Gamepad" + (masterIsGamepad1 ? "1" : "2")) : "You are only CLAUDIO");
            telemetry.addData("Speed", (scale==1.0) ? 4 : (scale==0.75) ? 3 : (scale==0.5) ? 2 : (scale==0.25) ? 1 : "Folle");
            telemetry.addData("Climbing mode", (climbingMode) ? "Activated" : "OFF");
            telemetry.addData("Climbing velocity", climbVelocity);
            telemetry.update();
        }
    }

    // =========================================================================================
    //                                    METODI CONDIVISI
    // =========================================================================================
    // Questi metodi contengono la logica che prima era scritta due volte (una per Claudio Mode
    // e una per God Mode). Ora vengono chiamati da entrambe le modalità nel loop principale,
    // passando come parametri i valori dei tasti letti dal gamepad giusto per quella modalità.
    // Se in futuro serve cambiare il comportamento di un meccanismo, basta modificarlo QUI:
    // la modifica si applica automaticamente sia a Claudio Mode che a God Mode.

    /**
     * Calcola leftPower/rightPower per la guida Arcade (throttle + sterzo), con normalizzazione
     * se la somma supera 1.0. Scrive direttamente nei campi leftPower/rightPower.
     *
     * @param throttle avanti/indietro, già con il segno giusto (tipicamente -stick_y)
     * @param spin     sterzo, tipicamente right_stick_x
     */
    private void driveArcade(double throttle, double spin){
        leftPower = throttle + spin;
        rightPower = throttle - spin;
        double max = Math.max(Math.abs(leftPower), Math.abs(rightPower));
        if (max > 1.0){
            leftPower /= max;
            rightPower /= max;
        }
    }

    /**
     * Gestisce il cambio marcia (scale) con i grilletti del gamepad passato, applica la scala
     * a leftPower/rightPower e li invia ai motori di trazione. Usato sia da gamepad1 (Claudio
     * Mode) che dal master (God Mode) perché il mapping marce è identico nelle due modalità.
     *
     * @param g gamepad da cui leggere i grilletti (gamepad1 in Claudio Mode, master in God Mode)
     */
    private void applyGearAndDrive(Gamepad g){
        boolean currentRightTrigger = g.right_trigger > 0.5;
        boolean currentLeftTrigger = g.left_trigger > 0.5;

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
    }

    /**
     * Gestisce il toggle del Climbing Mode (tasto X) e la regolazione della velocità di
     * climbing (dpad su/giù). Stessa logica e stessi tasti sia in Claudio Mode (gamepad1)
     * che in God Mode (master), quindi un solo metodo basta per entrambe le modalità.
     *
     * @param g gamepad da cui leggere X e dpad su/giù
     */
    private void handleClimbing(Gamepad g){
        boolean xStateActual = g.x;

        if (xStateActual && !xStateBefore){
            climbingMode = !climbingMode;
        }

        xStateBefore = xStateActual;

        if (climbingMode){
            climbMotorInt.setPower(climbVelocity);
            climbMotorEst.setPower(climbVelocity);
        } else {
            climbMotorInt.setPower(0);
            climbMotorEst.setPower(0);
        }

        boolean currentUpDpad = g.dpad_up;
        boolean currentDownDpad = g.dpad_down;

        if (currentUpDpad && !lastUpDpad && climbVelocity < 0.9){
            climbVelocity += .1;
        }

        if (currentDownDpad && !lastDownDpad && climbVelocity > -0.9){
            climbVelocity -= .1;
        }

        lastDownDpad = currentDownDpad;
        lastUpDpad = currentUpDpad;
    }

    /**
     * Gestisce TUTTI i meccanismi: toggle servo, sparo, intake/outtake, e i due stati del
     * flywheel (Idle e Full Speed). I tasti non vengono letti direttamente da un Gamepad qui
     * dentro: vengono passati già come booleani dal chiamante, perché Claudio Mode e God Mode
     * usano tasti leggermente diversi per Idle/Full Speed (per evitare conflitti con altri
     * comandi sullo stesso gamepad in God Mode). Così la LOGICA resta unica, mentre il MAPPING
     * dei tasti resta libero di variare tra le due modalità.
     *
     * Priorità di stato del flywheel (dall'alto in basso):
     *   1) Full Speed attivo  -> rampa fino a TARGET_VELOCITY (2000)
     *   2) Idle attivo        -> velocità fissa IDLE_VELOCITY (900), senza rampa
     *   3) nessuno dei due     -> velocità 0 (a meno che si stia tenendo premuto outtakeBtn,
     *                             che spinge il flywheel a -500 per liberare palline incastrate)
     *
     * Lo sparo (shootBtn) è abilitato solo se velocityok è vera, cioè se il Full Speed è
     * attivo e ENTRAMBI i flywheel hanno superato 1700 RPM. Una volta vera, velocityok resta
     * vera finché il Full Speed non viene disattivato (si azzera subito quando si spegne).
     *
     * @param shootBtn        tasto sparo (A)
     * @param servoToggleBtn  tasto toggle servo (B)
     * @param idleToggleBtn   tasto toggle Idle (X in Claudio Mode, dpad_left in God Mode)
     * @param fullSpeedToggleBtn tasto toggle Full Speed (Y in entrambe le modalità)
     * @param intakeBtn       tasto intake (right bumper)
     * @param outtakeBtn      tasto outtake (left bumper)
     */
    private void handleMechanisms(boolean shootBtn, boolean servoToggleBtn, boolean idleToggleBtn,
                                  boolean fullSpeedToggleBtn, boolean intakeBtn, boolean outtakeBtn){

        // --- Toggle servo (aperto/chiuso quando non si sta sparando o facendo intake/outtake) ---
        if (servoToggleBtn && !bStateBeforeServo){
            servoToggleOpen = !servoToggleOpen;
        }
        bStateBeforeServo = servoToggleBtn;

        // --- Controllo velocityok: si azzera appena si spegne il Full Speed, diventa vera
        //     quando entrambi i flywheel superano 1700 RPM con il Full Speed attivo, e resta
        //     vera finché il Full Speed resta attivo (anche se poi la velocità scende un po'). ---
        if (!flywheelFullSpeed){
            velocityok = false;
        }
        if (flywheel_right.getVelocity() > 1700 && flywheel_left.getVelocity() > 1700 && flywheelFullSpeed){
            velocityok = true;
        }

        // --- Sparo / Intake / Outtake / stato di riposo ---
        if (shootBtn && velocityok) {
            servitoreRight.setPosition(SERVO_SHOOT);
            servitoreLeft.setPosition(SERVO_SHOOT);
            upIntakeMotor.setPower(-0.8);
            upIntakeSlowMotor.setPower(-0.8);
        }
        else if (intakeBtn) {
            servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
            servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
            upIntakeMotor.setPower(-1);
            upIntakeSlowMotor.setPower(-1);
        }
        else if (outtakeBtn) {
            servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
            servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
            upIntakeMotor.setPower(1);
            upIntakeSlowMotor.setPower(1);
            // mentre si fa outtake, il flywheel viene spinto all'indietro per aiutare a
            // espellere eventuali palline incastrate
            flywheel_left.setVelocity(-500);
            flywheel_right.setVelocity(-500);
        }
        else {
            servitoreRight.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
            servitoreLeft.setPosition(servoToggleOpen ? SERVO_OPEN : SERVO_CLOSE);
            upIntakeMotor.setPower(0);
            upIntakeSlowMotor.setPower(0);
        }

        // --- Toggle Idle ---
        if (idleToggleBtn && !xStateBeforeG2){
            flywheelActivate = !flywheelActivate;
        }
        xStateBeforeG2 = idleToggleBtn;

        // --- Toggle Full Speed ---
        if (fullSpeedToggleBtn && !yStateBefore){
            flywheelFullSpeed = !flywheelFullSpeed;
        }
        yStateBefore = fullSpeedToggleBtn;

        // --- Applica lo stato scelto al flywheel (priorità: Full Speed > Idle > spento) ---
        if (flywheelFullSpeed) {
            int filteredFullVelocity = update(TARGET_VELOCITY);
            flywheel_right.setVelocity(filteredFullVelocity);
            flywheel_left.setVelocity(filteredFullVelocity);
        } else if (flywheelActivate){
            flywheel_right.setVelocity(IDLE_VELOCITY);
            flywheel_left.setVelocity(IDLE_VELOCITY);
        } else if (!outtakeBtn){
            // non azzerare se si sta tenendo premuto outtake, altrimenti si sovrascriverebbe
            // subito il -500 impostato qui sopra
            flywheel_right.setVelocity(0);
            flywheel_left.setVelocity(0);
        }
    }

    /**
     * Rampa la potenza del flywheel verso targetPower di al massimo maxStep unità per ciclo,
     * per evitare sbalzi di corrente troppo bruschi quando si passa da fermo a Full Speed.
     */
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