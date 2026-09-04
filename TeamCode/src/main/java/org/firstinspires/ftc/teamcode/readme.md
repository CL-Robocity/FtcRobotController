# TeleOpMovements_NewControl

Italiano sotto ogni sezione, traduzione inglese subito dopo.
Italian below each section, English translation right after.

## Panoramica

Questo OpMode TeleOp per FTC implementa due modalità di controllo: Claudio Mode (due giocatori) e God Mode (un gamepad prende il controllo totale). Si passa dall'una all'altra al volo con una combo di tasti.

## Overview

This FTC TeleOp OpMode implements two control modes: Claudio Mode (two players) and God Mode (one gamepad takes full control). You switch between them on the fly with a button combo.

---

## Claudio Mode (modalità di default)

Gamepad1 guida il robot, gamepad2 controlla i meccanismi (intake, flywheel, servo).

## Claudio Mode (default mode)

Gamepad1 drives the robot, gamepad2 controls the mechanisms (intake, flywheel, servo).

---

## God Mode

Si attiva premendo L3+R3 insieme su uno dei due gamepad. Chi preme la combo diventa il "master" e prende il controllo di guida e meccanismi; l'altro gamepad viene disattivato del tutto. Si esce premendo di nuovo la combo su uno qualsiasi dei due gamepad. La Uster Mode non è disponibile in God Mode, solo in Claudio Mode.

## God Mode

Activated by pressing L3+R3 together on either gamepad. Whoever presses the combo becomes the "master" and takes over both driving and mechanisms; the other gamepad is fully disabled. You exit by pressing the combo again on either gamepad. Uster Mode is not available in God Mode, only in Claudio Mode.

---

## Tasti - Claudio Mode, Gamepad1 (guida)

- Stick sinistro (Y): avanti/indietro (Arcade) o motore SX (Uster)
- Stick destro: sterzo (Arcade) o motore DX (Uster)
- B (cerchio): toggle Uster Mode / Arcade Mode
- X (quadrato): toggle Climbing Mode
- D-pad su/giù: regola la velocità di climbing
- L2/R2: marcia giù/su (scala di potenza)
- L3+R3: attiva God Mode

## Buttons - Claudio Mode, Gamepad1 (driver)

- Left stick (Y): forward/back (Arcade) or left motor (Uster)
- Right stick: steering (Arcade) or right motor (Uster)
- B (circle): toggle Uster Mode / Arcade Mode
- X (square): toggle Climbing Mode
- D-pad up/down: adjust climb speed
- L2/R2: gear down/up (power scale)
- L3+R3: activate God Mode

---

## Tasti - Claudio Mode, Gamepad2 (meccanismi)

- A (croce): sparo, apre i servo e attiva l'intake (solo se il flywheel supera 1800 RPM)
- B (cerchio): toggle servo aperto/chiuso
- Y (triangolo): toggle flywheel idle (900 RPM)
- X (quadrato): toggle flywheel full speed (2000 RPM)
- D-pad su: eject del flywheel se è quasi fermo (RPM < 100), lo spinge indietro a -500 e apre i servo
- R1: intake
- L1: outtake
- L3+R3: attiva God Mode

## Buttons - Claudio Mode, Gamepad2 (operator)

- A (cross): shoot, opens the servos and runs the intake (only if the flywheel is above 1800 RPM)
- B (circle): toggle servo open/closed
- Y (triangle): toggle flywheel idle (900 RPM)
- X (square): toggle flywheel full speed (2000 RPM)
- D-pad up: eject the flywheel if it's nearly stopped (RPM < 100), pushes it backward to -500 and opens the servos
- R1: intake
- L1: outtake
- L3+R3: activate God Mode

---

## Tasti - God Mode (gamepad master)

Stessa disposizione di guida di gamepad1, ma con i meccanismi rimappati per non entrare in conflitto con climbing e regolazione velocità di climbing (che restano su X e D-pad su/giù).

- Stick sinistro/destro: guida Arcade
- A: sparo (solo se flywheel > 1800 RPM)
- B: toggle servo aperto/chiuso
- X: toggle Climbing Mode
- Y: toggle flywheel idle
- D-pad sinistra: toggle flywheel full speed
- D-pad destra: eject flywheel
- D-pad su/giù: regola velocità di climbing
- R1: intake
- L1: outtake, spinge anche il flywheel indietro a -0.5
- L2/R2: marcia giù/su
- L3+R3: disattiva God Mode

## Buttons - God Mode (master gamepad)

Same driving layout as gamepad1, but mechanisms are remapped to avoid conflicting with climbing and climb speed adjustment (which stay on X and D-pad up/down).

- Left/right stick: Arcade drive
- A: shoot (only if flywheel > 1800 RPM)
- B: toggle servo open/closed
- X: toggle Climbing Mode
- Y: toggle flywheel idle
- D-pad left: toggle flywheel full speed
- D-pad right: eject flywheel
- D-pad up/down: adjust climb speed
- R1: intake
- L1: outtake, also pushes flywheel backward at -0.5
- L2/R2: gear down/up
- L3+R3: deactivate God Mode

---

## Logica del flywheel

Tre stati possibili, in ordine di priorità:

1. Full speed (se attivo): sale gradualmente fino a 2000 tramite la rampa di `update()`, passo massimo 16 per ciclo.
2. Idle (se attivo e full speed spento): 900 fisso, senza rampa.
3. Spento: 0, a meno che sia in corso un eject.

L'eject è pensato per liberare palline incastrate: parte solo se il flywheel è quasi fermo e lo spinge in senso opposto per un istante.

Lo sparo (tasto A) controlla `flywheel_left.getVelocity() > 1800` indipendentemente da idle/full speed, così parte solo quando il flywheel è davvero a regime.

## Flywheel logic

Three possible states, in priority order:

1. Full speed (if active): ramps up gradually to 2000 via `update()`, max step of 16 per loop.
2. Idle (if active and full speed is off): fixed at 900, no ramping.
3. Off: 0, unless an eject is in progress.

The eject is meant to clear jammed balls: it only fires if the flywheel is nearly stopped, pushing it in reverse briefly.

Shooting (A button) checks `flywheel_left.getVelocity() > 1800` regardless of idle/full speed, so it only fires once the flywheel is actually up to speed.

---

## Costanti principali

- TARGET_VELOCITY = 2000 (flywheel full speed)
- IDLE_VELOCITY = 900 (flywheel idle)
- SERVO_CLOSE = 0.01
- SERVO_OPEN = 0.22
- SERVO_SHOOT = 0.12
- maxStep = 16 (rampa massima per ciclo)

## Main constants

- TARGET_VELOCITY = 2000 (flywheel full speed)
- IDLE_VELOCITY = 900 (flywheel idle)
- SERVO_CLOSE = 0.01
- SERVO_OPEN = 0.22
- SERVO_SHOOT = 0.12
- maxStep = 16 (max ramp per loop)

---

## Hardware richiesto (nomi di configurazione)

left_motor, right_motor, intake_motor, second_intake_motor, flywheel_left, flywheel_right (DcMotorEx con encoder), servitore_1, servitore_2, climb_motor_int, climb_motor_est.

## Required hardware (configuration names)

left_motor, right_motor, intake_motor, second_intake_motor, flywheel_left, flywheel_right (DcMotorEx with encoder), servitore_1, servitore_2, climb_motor_int, climb_motor_est.

---

## Note

Tutti i toggle (Uster, Climbing, Flywheel Idle, Flywheel Full Speed, Servo, God Mode) usano edge detection standard: scattano una volta sola per pressione, non sfarfallano se il tasto resta premuto.

In God Mode la Uster Mode è esclusa di proposito, si guida solo in Arcade. Il mapping dei meccanismi in God Mode è stato scelto per non sovrapporsi ai comandi di guida/climbing già su gamepad1; se serve un mapping diverso basta cercare i riferimenti a `master.` nel codice.

## Notes

All toggles (Uster, Climbing, Flywheel Idle, Flywheel Full Speed, Servo, God Mode) use standard edge detection: they fire once per press, no flickering if the button stays held.

Uster Mode is intentionally left out of God Mode, driving is Arcade-only there. The God Mode mechanism mapping was chosen to avoid overlapping with the drive/climbing controls already on gamepad1; if a different mapping is needed, just search for `master.` references in the code.

---
---

# V2 - Aggiornamenti

Questa sezione descrive cosa e' cambiato rispetto alla versione precedente del documento (sopra). Il testo v1 resta com'era per riferimento storico; qui sotto solo le differenze e le novita'.

# V2 - Updates

This section describes what changed compared to the previous version of this document (above). The v1 text stays as-is for historical reference; below are only the differences and new features.

---

## Novita' principali / Main changes

Italiano:

- Aggiunto un controllo unico di velocita' prima dello sparo: la variabile `velocityok` diventa vera solo quando entrambi i flywheel superano 1700 RPM con il Full Speed attivo, e resta vera finche' il Full Speed non viene disattivato. Lo sparo (tasto A) ora richiede `velocityok`, non piu' una soglia diretta sul singolo motore.
- Lo sparo ora porta i servo su `SERVO_SHOOT` (0.12) invece di `SERVO_OPEN`, e la potenza dell'intake durante lo sparo e' -0.8 invece di -1.
- Tenendo premuto outtake (left bumper), oltre a invertire l'intake, il flywheel viene spinto a -500 per aiutare a liberare palline incastrate. Questo vale ora sia in Claudio Mode che in God Mode.
- I tasti per Idle e Full Speed del flywheel sono stati scambiati: ora Y attiva/disattiva il Full Speed e X (in Claudio Mode) o D-pad sinistra (in God Mode) attiva/disattiva l'Idle. In God Mode X resta occupato dal Climbing, per questo li' l'Idle e' su D-pad sinistra invece che su X.
- La funzione di Eject (che spingeva il flywheel a -500 con D-pad su/destra quando quasi fermo) e' stata rimossa in entrambe le modalita'.
- `SERVO_OPEN` e' stato modificato da 0.22 a 0.17.
- Aggiunta una riga di telemetria "Velocity OK" per vedere a colpo d'occhio se lo sparo e' abilitato.

English:

- Added a single velocity check before shooting: the `velocityok` variable becomes true only when both flywheels exceed 1700 RPM with Full Speed active, and stays true until Full Speed is turned off. Shooting (A button) now requires `velocityok`, instead of a direct threshold on a single motor.
- Shooting now moves the servos to `SERVO_SHOOT` (0.12) instead of `SERVO_OPEN`, and intake power during shooting is -0.8 instead of -1.
- Holding outtake (left bumper), besides reversing the intake, now also pushes the flywheel to -500 to help clear jammed balls. This now applies to both Claudio Mode and God Mode.
- The Idle and Full Speed flywheel buttons were swapped: Y now toggles Full Speed, and X (Claudio Mode) or D-pad left (God Mode) toggles Idle. In God Mode, X is still used for Climbing, which is why Idle was moved to D-pad left there.
- The Eject function (which pushed the flywheel to -500 via D-pad up/right when nearly stopped) has been removed in both modes.
- `SERVO_OPEN` was changed from 0.22 to 0.17.
- Added a "Velocity OK" telemetry line to see at a glance whether shooting is enabled.

---

## Tasti aggiornati - Claudio Mode, Gamepad2 (meccanismi) / Updated buttons - Claudio Mode, Gamepad2 (mechanisms)

Italiano:

- A (croce): sparo, apre i servo su SERVO_SHOOT e attiva l'intake a -0.8 (solo se `velocityok` e' vera)
- B (cerchio): toggle servo aperto/chiuso
- X (quadrato): toggle flywheel idle (900 RPM)
- Y (triangolo): toggle flywheel full speed (2000 RPM)
- R1: intake
- L1: outtake, spinge anche il flywheel a -500
- Eject rimosso (non piu' presente su D-pad)

English:

- A (cross): shoot, moves the servos to SERVO_SHOOT and runs intake at -0.8 (only if `velocityok` is true)
- B (circle): toggle servo open/closed
- X (square): toggle flywheel idle (900 RPM)
- Y (triangle): toggle flywheel full speed (2000 RPM)
- R1: intake
- L1: outtake, also pushes the flywheel to -500
- Eject removed (no longer on D-pad)

---

## Tasti aggiornati - God Mode (gamepad master) / Updated buttons - God Mode (master gamepad)

Italiano:

- A: sparo, stessa logica di Claudio Mode (richiede `velocityok`)
- B: toggle servo aperto/chiuso
- X: toggle Climbing Mode (invariato)
- Y: toggle flywheel full speed
- D-pad sinistra: toggle flywheel idle
- D-pad su/giu: regola velocita' di climbing (invariato)
- L1: outtake, spinge anche il flywheel a -500
- R1: intake
- Eject rimosso (non piu' presente su D-pad destra)

English:

- A: shoot, same logic as Claudio Mode (requires `velocityok`)
- B: toggle servo open/closed
- X: toggle Climbing Mode (unchanged)
- Y: toggle flywheel full speed
- D-pad left: toggle flywheel idle
- D-pad up/down: adjust climb speed (unchanged)
- L1: outtake, also pushes the flywheel to -500
- R1: intake
- Eject removed (no longer on D-pad right)

---

## Logica del flywheel aggiornata / Updated flywheel logic

Italiano:

Le priorita' restano le stesse (Full Speed > Idle > spento), ma con due differenze:

- Quando ne' Full Speed ne' Idle sono attivi, il flywheel va a 0 a meno che si stia tenendo premuto outtake, nel qual caso resta a -500 (invece che essere subito sovrascritto a 0).
- Lo sparo non guarda piu' la velocita' istantanea al momento della pressione di A, ma la variabile `velocityok`, calcolata ogni ciclo prima del controllo dei tasti: si azzera appena il Full Speed si spegne, e diventa vera (restando tale) quando entrambi i motori superano 1700 RPM.

English:

The priority order stays the same (Full Speed > Idle > off), with two differences:

- When neither Full Speed nor Idle is active, the flywheel goes to 0 unless outtake is being held, in which case it stays at -500 (instead of being immediately overwritten to 0).
- Shooting no longer checks the instantaneous velocity at the moment A is pressed; instead it checks the `velocityok` variable, computed every loop before reading the buttons: it resets to false as soon as Full Speed is turned off, and becomes true (and stays true) once both motors exceed 1700 RPM.

---

## Refactoring del codice / Code refactoring

Italiano:

La struttura interna del programma e' stata riorganizzata per eliminare la duplicazione tra Claudio Mode e God Mode. Prima, guida, climbing e meccanismi erano scritti due volte (una copia per modalita'), con il rischio di modificare una copia e dimenticare l'altra. Ora la logica vive in metodi condivisi, richiamati da entrambe le modalita':

- `driveArcade(throttle, spin)`: calcola le potenze motore per la guida Arcade.
- `applyGearAndDrive(Gamepad g)`: gestisce il cambio marcia e invia la potenza ai motori di trazione.
- `handleClimbing(Gamepad g)`: gestisce il toggle Climbing e la regolazione della sua velocita'. Stessi tasti in entrambe le modalita' (X, D-pad su/giu), quindi basta passare il gamepad giusto.
- `handleMechanisms(...)`: gestisce servo, sparo, intake/outtake e flywheel. Non riceve un gamepad intero ma i singoli tasti gia' letti dal chiamante, perche' Idle e Full Speed usano tasti diversi tra le due modalita' (per evitare conflitti in God Mode, dove climbing e meccanismi condividono lo stesso gamepad).

Nel loop principale, due variabili (`driver` e `operator`) puntano al gamepad giusto a seconda della modalita' attiva; le chiamate ai metodi condivisi restano identiche, cambia solo quale gamepad fisico viene passato.

Comportamento a runtime: nessuna modifica rispetto a prima del refactoring, a parita' di funzionalita' descritte sopra. Cambia solo l'organizzazione interna del codice, per rendere piu' facile mantenere le due modalita' allineate in futuro.

English:

The program's internal structure was reorganized to eliminate duplication between Claudio Mode and God Mode. Previously, driving, climbing and mechanisms were each written twice (one copy per mode), risking that one copy gets updated while the other is forgotten. Now the logic lives in shared methods, called by both modes:

- `driveArcade(throttle, spin)`: computes motor power for Arcade driving.
- `applyGearAndDrive(Gamepad g)`: handles gear shifting and sends power to the drive motors.
- `handleClimbing(Gamepad g)`: handles the Climbing toggle and speed adjustment. Same buttons in both modes (X, D-pad up/down), so it just needs the right gamepad passed in.
- `handleMechanisms(...)`: handles servo, shooting, intake/outtake and the flywheel. It doesn't take a whole gamepad, but individual button values already read by the caller, because Idle and Full Speed use different buttons between the two modes (to avoid conflicts in God Mode, where climbing and mechanisms share the same gamepad).

In the main loop, two variables (`driver` and `operator`) point to the correct gamepad depending on the active mode; calls to the shared methods stay identical, only which physical gamepad gets passed in changes.

Runtime behavior: unchanged compared to before the refactor, given the same features described above. Only the internal code organization changes, to make it easier to keep the two modes in sync going forward.

---

## Costanti aggiornate / Updated constants

- SERVO_OPEN = 0.17 (era 0.22 / was 0.22)
- SERVO_SHOOT = 0.12 (nuova costante, usata solo durante lo sparo / new constant, used only while shooting)
- TARGET_VELOCITY, IDLE_VELOCITY, SERVO_CLOSE, maxStep: invariati / unchanged