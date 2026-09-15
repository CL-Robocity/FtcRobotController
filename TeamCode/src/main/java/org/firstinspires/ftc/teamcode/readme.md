# TeleOpMovements_NewControl

Questo documento e' diviso in due parti: prima l'intero README in italiano, poi l'intero README in inglese (traduzione integrale).

This document is split into two parts: the full README in Italian first, then the full README in English (complete translation).

---
---

# ITALIANO

## 1. Panoramica del progetto

Questo OpMode TeleOp per FTC implementa due modalita' di controllo: Claudio Mode (due giocatori, di default) e God Mode (un solo gamepad prende il controllo totale). Si passa dall'una all'altra al volo con la combo L3+R3, premibile da entrambi i gamepad.

Il robot gestisce: guida (Arcade o Uster), cambio marcia a step, un sistema di intake/outtake a doppio motore, un flywheel a doppia velocita' con controllo di sicurezza prima dello sparo, due servo "servitori" per il rilascio delle palline, uno slider a due posizioni fisse, un sistema di climbing a velocita' regolabile, e un tentativo (non funzionante) di feedback sonoro.

---

## 2. Changelog Hardware/Software

Changelog completo, in ordine cronologico, ricostruito dai commenti in testa al file sorgente. Ogni versione e' cumulativa rispetto alla precedente.

### 3/9/26

**SW:** aggiunto controllo indipendente dell'apertura servo; aggiunta rotazione dello slowintake motor durante l'intake; aggiunta rotazione inversa del flywheel durante l'outtake; modificato l'utilizzo della idle velocity (rimane nel codice ma non piu' necessaria).
**HW:** modificata l'altezza del primo rullo di intake; aggiunti due pezzi per il blocco/incastro delle palline sia sul buco centrale superiore che sui buchi laterali del flywheel; prima prova dello slider.

### 4/9/26

**SW:** aggiunto un controllo unico prima dello sparo a 1700 giri; implementate tutte le funzioni sia in God Mode che in Claudio Mode; modificate le velocita' di outtake e flywheel a sparo; implementati i servo slider (slider_left, slider_right) e il relativo handle; implementate le posizioni SOUT 0.22 e SIN 0.00. Refactor: la logica di meccanismi, climbing e guida (duplicata quasi identica tra le due modalita') e' stata spostata in metodi condivisi unici (`handleMechanisms`, `handleClimbing`, `driveArcade`, `applyGearAndDrive`), chiamati da entrambe le modalita' passando i tasti giusti — nessun cambio di comportamento a runtime, solo di organizzazione del codice.
**HW:** aggiunto un piccolo pezzo di policarbonato sotto l'intake motor per evitare l'incastro delle palline; i pezzi di appoggio al muro antibattuta sono stati portati alla misura massima per contenere le palline sotto il canestro, poi riportati indietro; implementazione fisica dello slider.

### 7/9/26

**SW:** nessuna modifica.
**HW:** alzati rulli e motori per evitare l'incastro delle palline in outtake.

### 9/9/26 (prima modifica)

**HW:** rivisitazione storica dell'intake, con aggiunta dello slider e altre piccole modifiche.

### 9/9/26 (seconda modifica)

**SW:** rese completamente parametriche, per la rimappatura del controller, tutte le funzioni `handle...`; tentativo di implementazione dei suoni (non funzionante, vedi sezione SoundPlayer).
**HW:** aggiustate le altezze di flywheel e intake; modificata la rampa con l'aggiunta di una discesa; creata una rete sopra l'intake.

### 12/9/26

**SW:** aggiunta `handleSlider` con controllo di posizione tramite i trigger analogici dell'operatore: R2 incrementa la posizione verso SOUT, L2 la decrementa verso SIN, nessun trigger premuto = posizione bloccata all'ultimo valore. Aggiunta telemetria della posizione slider in tempo reale. *(Questo comportamento analogico e' stato poi sostituito il giorno seguente, vedi 13/9.)*
**HW:** slider finito ma non funzionante insieme all'intake; tentativo di sparo in retromarcia con il vecchio drivetrain.

### 13/9/26

**SW:** aggiunto un controllo di sicurezza per il flywheel legato all'outtake (vedi sezione Flywheel); modificati SIN e SOUT per le nuove lunghezze dei servo e lo slider e' tornato ad essere un toggle (rimossa la funzione ausiliaria a trigger analogici introdotta il giorno prima); tutte le funzioni sono state inserite dentro `if (fullController)` in modo da avere due mappe controlli distinte e leggibili per God Mode e Claudio Mode.
**HW:** aggiunto plexiglass frontale per la chiusura parziale dell'intake anche da aperto (manca ancora la rete); abbassato uno dei 4 pezzi della rampa per arrampicarsi meglio; rifatti gli zero di entrambi i servo dello slider.

### Modifiche rilevate nell'ultima lettura del codice, non ancora annotate nei commenti di changelog

Confrontando l'ultima versione del sorgente con la precedente sono emerse alcune differenze funzionali che non risultano descritte nei commenti in testa al file. Vanno tenute presenti perche' cambiano il comportamento reale del robot:

- **Outtake "sicuro" con flywheel fermo:** la potenza di `upIntakeMotor` e' passata da 0.4 a **0.9**, quella di `upIntakeSlowMotor` da 0.7 a **0.2**, e la velocita' di retromarcia del flywheel da -600 a **-400**.
- **Outtake con flywheel in moto:** prima veniva azionato solo `upIntakeSlowMotor` a 0.9; ora viene azionato `upIntakeMotor` a 0.9 — e' cambiato quale motore lavora, non solo la potenza. Vedi sezione 4.2 per il dettaglio.
- **Telemetria "Slider position":** rimossa. In precedenza mostrava un valore statico mai aggiornato (bug), ora la riga non c'e' proprio piu'.
- **Javadoc di `handleSlider`:** il commento che descriveva il controllo a trigger analogici e' stato svuotato; il metodo ora non ha alcuna documentazione nel codice (vedi sezione 8.5).

### Riepilogo modifiche di valore rispetto alla documentazione storica

| Elemento | Prima (documentato in passato) | Attuale |
|---|---|---|
| Soglia `velocityok` | > 1700 RPM (nei commenti) | 1750 RPM (nel codice, invariato da tempo) |
| Slider | toggle (X / touchpad) | toggle (X / touchpad) — invariato, dopo una parentesi a trigger analogici il 12/9 poi annullata |
| Outtake sicuro (flywheel fermo) | intake 0.4 / slowintake 0.7 / flywheel -600 | intake 0.9 / slowintake 0.2 / flywheel -400 |
| Outtake con flywheel in moto | azionava upIntakeSlowMotor a 0.9 | aziona upIntakeMotor a 0.9 |
| Telemetria slider | mostrava un valore statico non aggiornato | rimossa |
| Schermata di init | — | "Jolly Roger" rimosso (metodo presente ma non piu' chiamato), messaggio semplificato |
| SoundPlayer | non menzionato | import commentato, tentativo non riuscito |

---

## 3. Mappatura Comandi

### Claudio Mode — Gamepad 1 (Guida)

| Comando | Funzione |
|---|---|
| Stick sinistro (Y) | Avanti/indietro (Arcade) oppure motore SX (Uster) |
| Stick destro | Sterzo (Arcade) oppure motore DX (Uster) |
| B (cerchio) | Toggle Uster Mode / Arcade Mode |
| X (quadrato) | Toggle Climbing Mode |
| D-pad su / giu' | Aumenta / diminuisce la velocita' di climbing (±0.1, range -0.9…0.9) |
| L2 / R2 | Marcia giu' / su (scala di potenza, step 0.25, range 0.25…1.0) |
| L3+R3 | Attiva God Mode (master = gamepad1) |

### Claudio Mode — Gamepad 2 (Meccanismi)

| Comando | Funzione |
|---|---|
| A (croce) | Sparo: porta i servo a SERVO_SHOOT e attiva l'intake a -0.8 (solo se `velocityok` e' vera) |
| B (cerchio) | Toggle servo aperto/chiuso |
| X (quadrato) | Toggle slider aperto/chiuso |
| Y (triangolo) | Toggle flywheel Full Speed (2000 RPM) |
| Touchpad | Toggle flywheel Idle (900 RPM) |
| R1 | Intake |
| L1 | Outtake "sicuro" (vedi sezione 4.2) |
| L3+R3 | Attiva God Mode (master = gamepad2) |

### God Mode (gamepad master, attivato con L3+R3)

Un solo gamepad controlla guida e meccanismi. La disposizione ricalca il Gamepad 1 per la guida, ma i meccanismi sono rimappati per non entrare in conflitto con climbing (che resta su X e D-pad su/giu').

| Comando | Funzione |
|---|---|
| Stick sinistro/destro | Guida Arcade (Uster Mode non disponibile) |
| A | Sparo (richiede `velocityok`) |
| B | Toggle servo aperto/chiuso |
| X | Toggle Climbing Mode |
| Y | Toggle flywheel Full Speed |
| R3 (stick destro premuto) | Toggle flywheel Idle |
| Touchpad | Toggle slider aperto/chiuso |
| D-pad su / giu' | Regola velocita' di climbing |
| R1 | Intake |
| L1 | Outtake "sicuro" |
| L2 / R2 | Marcia giu' / su |
| L3+R3 | Disattiva God Mode |

---

## 4. Sistemi Principali

### 4.1 Guida: Arcade vs Uster

L'Arcade Drive (`driveArcade`) calcola `leftPower`/`rightPower` da throttle + sterzo, normalizzando se la somma supera 1.0. E' l'unica modalita' disponibile in God Mode. La Uster Mode (doppio stick stile tank, un motore per stick) esiste solo in Claudio Mode ed e' attivabile con B su gamepad1. In entrambi i casi, `applyGearAndDrive` applica poi la marcia (`scale`) e invia la potenza ai motori.

### 4.2 Flywheel a doppia velocita' e sicurezza di sparo

Tre stati possibili, in ordine di priorita':

1. **Full Speed** (se attivo): sale gradualmente fino a `TARGET_VELOCITY` (2000) tramite la rampa del metodo `update()`, passo massimo 16 per ciclo, per evitare sbalzi di corrente.
2. **Idle** (se attivo e Full Speed spento): velocita' fissa `IDLE_VELOCITY` (900), senza rampa.
3. **Spento**: velocita' 0, a meno che sia in corso un outtake con flywheel fermo (vedi sotto).

La variabile `velocityok` abilita lo sparo (tasto A): diventa vera solo quando entrambi i flywheel superano 1750 RPM con Full Speed attivo, e resta vera finche' Full Speed non viene disattivato (si azzera immediatamente allo spegnimento). Lo sparo non guarda piu' la velocita' istantanea al momento della pressione di A, ma questa variabile calcolata ogni ciclo. (Nota: alcuni commenti nel codice citano ancora la vecchia soglia di 1700 RPM — vedi sezione 8.5.)

**Outtake "sicuro":** tenendo L1, il comportamento dipende dallo stato del flywheel:

- Flywheel **fermo** (Idle e Full Speed entrambi spenti, velocita' < 50 RPM su entrambi i motori): `upIntakeMotor` va a **0.9**, `upIntakeSlowMotor` a **0.2**, e il flywheel viene spinto a **-400** su entrambi i lati per aiutare a liberare palline incastrate.
- Flywheel **in moto** (Idle o Full Speed attivi, oppure velocita' > 50 RPM su almeno un motore): viene azionato solo `upIntakeMotor` a 0.9, senza toccare il flywheel ne' `upIntakeSlowMotor` — questo evita di lanciare palline in direzione sbagliata se si preme outtake per errore mentre il flywheel e' a regime.

### 4.3 Slider

Due servo (`slider_dx` → `sliderRight`, `slider_sx` → `sliderLeft`, quest'ultimo con direzione invertita) controllati come toggle tra due posizioni fisse: `SIN` (dentro, 0.86) e `SOUT` (fuori, 0.0). Attivabile con X su gamepad2 in Claudio Mode, o con il touchpad del master in God Mode.

Nota storica: il 12/9 era stato provato un controllo analogico (R2 per aprire, L2 per chiudere, con posizione intermedia libera), poi abbandonato il giorno dopo tornando al toggle a due posizioni. Il Javadoc del metodo `handleSlider` che descriveva quel comportamento e' stato nel frattempo svuotato (vedi sezione 8.5): il metodo attualmente non ha commenti nel codice, ma il comportamento reale resta quello a toggle descritto qui.

### 4.4 Climbing

Toggle attivabile con X (stesso tasto sul driver in entrambe le modalita', condiviso tramite `handleClimbing`). Quando attivo, entrambi i motori di climbing (`climb_motor_int`, `climb_motor_est`) girano a `climbVelocity` (default 0.5, regolabile con D-pad su/giu' in step di 0.1, range -0.9…0.9). Quando disattivato, potenza a 0.

### 4.5 Servomotori (servitori)

`servitoreRight` e `servitoreLeft` sono i servo che trattengono/rilasciano le palline. Hanno tre posizioni possibili gestite da costanti: `SERVO_CLOSE` (0.0), `SERVO_OPEN` (0.2, usata durante intake/outtake a seconda del toggle) e `SERVO_SHOOT` (0.2, usata solo durante lo sparo). Il toggle aperto/chiuso (B) determina quale delle due posizioni "di riposo" viene usata fuori dallo sparo.

### 4.6 SoundPlayer (sperimentale, non funzionante)

Il 9/9 e' stato tentato l'utilizzo di `com.qualcomm.ftccommon.SoundPlayer` per aggiungere feedback sonoro (ad es. per confermare lo sparo o il cambio modalita'), ma il tentativo non ha funzionato: nel codice attuale l'import e' commentato (`//import com.qualcomm.ftccommon.SoundPlayer;`) e non c'e' alcuna chiamata attiva al sistema audio. Al momento il robot non produce alcun feedback sonoro; questa parte resta da riprendere in una futura iterazione.

---

## 5. Costanti principali

| Costante | Valore | Note |
|---|---|---|
| `TARGET_VELOCITY` | 2000 | Flywheel Full Speed |
| `IDLE_VELOCITY` | 900 | Flywheel Idle |
| `SERVO_CLOSE` | 0.0 | Posizione servo chiusa |
| `SERVO_OPEN` | 0.2 | Posizione servo aperta (intake/outtake) |
| `SERVO_SHOOT` | 0.2 | Posizione servo durante sparo |
| `SIN` | 0.86 | Slider dentro |
| `SOUT` | 0.0 | Slider fuori |
| `scale` (iniziale) | 0.75 | Marcia di partenza, range 0.25–1.0 |
| `climbVelocity` (iniziale) | 0.5 | Range -0.9…0.9 |
| `maxStep` (in `update()`) | 16 | Rampa massima flywheel per ciclo |
| Soglia `velocityok` | 1750 RPM | Su entrambi i flywheel, con Full Speed attivo |
| Soglia outtake sicuro | 50 RPM | Su entrambi i flywheel, per abilitare la retromarcia |
| Velocita' retromarcia outtake | -400 | Non e' una costante nominata, e' un valore scritto direttamente nel metodo (magic number) |

---

## 6. Hardware richiesto (nomi di configurazione)

`left_motor`, `right_motor`, `intake_motor`, `second_intake_motor`, `flywheel_left`, `flywheel_right` (DcMotorEx con encoder), `servitore_1` (→ servitoreRight, REVERSE), `servitore_2` (→ servitoreLeft, FORWARD), `slider_dx` (→ sliderRight, FORWARD), `slider_sx` (→ sliderLeft, REVERSE), `climb_motor_int`, `climb_motor_est`.

---

## 7. Note generali

- Tutti i toggle (Uster, Climbing, Flywheel Idle, Flywheel Full Speed, Servo, Slider, God Mode) usano edge detection standard: scattano una volta sola per pressione, non sfarfallano se il tasto resta premuto.
- In God Mode la Uster Mode e' esclusa di proposito, si guida solo in Arcade.
- La schermata di init e' stata semplificata: il "Jolly Roger" mostrato in passato e' stato rimosso dalla chiamata (il metodo esiste ancora nel codice ma non viene piu' invocato).
- Il sistema SoundPlayer e' presente solo come tentativo, con import disattivato: nessun suono viene attualmente riprodotto dal robot.

---

## 8. Documentazione tecnica del codice

Questa sezione descrive come e' organizzato il programma e come usare/estendere ciascuna funzione, per chi deve mettere mano al codice.

### 8.1 Architettura generale

Il programma e' un unico `LinearOpMode` con un solo ciclo principale (`while (opModeIsActive())`). Ad ogni iterazione del ciclo:

1. Viene letta la combo L3+R3 su entrambi i gamepad per attivare/disattivare `fullController` (God Mode) e determinare `masterIsGamepad1`.
2. Vengono calcolati tre riferimenti a `Gamepad`: `master` (il gamepad che ha attivato il God Mode), `driver` (chi guida in questo ciclo) e `operator` (chi comanda i meccanismi in questo ciclo). In Claudio Mode `driver = gamepad1` e `operator = gamepad2`; in God Mode entrambi puntano a `master`.
3. Viene gestita la guida (Arcade o Uster).
4. Nel ramo `if (fullController) { ... } else { ... }` vengono chiamati, con i tasti giusti per la modalita' attiva, i metodi condivisi: `applyGearAndDrive`, `handleClimbing`, `handleSlider`, `handleMechanisms`.
5. Viene aggiornata la telemetria.

Il punto chiave dell'architettura e' che **la logica vive una sola volta**, nei metodi condivisi descritti sotto; cio' che cambia tra Claudio Mode e God Mode e' solo *quali tasti* vengono passati come parametri nella chiamata. Se serve cambiare come si comporta un meccanismo, si modifica il metodo condiviso; se serve cambiare solo quale tasto lo attiva, si modifica la chiamata nel blocco `if (fullController) / else`.

### 8.2 Riferimento metodi

**`private void driveArcade(double throttle, double spin)`**
Calcola `leftPower` e `rightPower` per la guida Arcade sommando/sottraendo `throttle` e `spin`, poi normalizza se la somma dei valori assoluti supera 1.0. Scrive direttamente nei campi di classe `leftPower`/`rightPower`, non ritorna nulla. Va chiamato prima di `applyGearAndDrive`, perche' quest'ultimo legge quei due campi. Uso tipico: `driveArcade(-driver.left_stick_y, driver.right_stick_x)`.

**`private void applyGearAndDrive(boolean shiftUpBtn, boolean shiftDownBtn)`**
Gestisce il cambio marcia: se `shiftUpBtn` passa da falso a vero (edge detection tramite `lastRightTrigger`), aumenta `scale` di 0.25 fino a un massimo di 1.0; se `shiftDownBtn` passa da falso a vero (tramite `lastLeftTrigger`), diminuisce `scale` di 0.25 fino a un minimo di 0.25. Applica poi `scale` a `leftPower`/`rightPower` e li invia con `setPower()` ai motori di trazione. Va chiamato dopo `driveArcade` (o dopo aver impostato `leftPower`/`rightPower` manualmente, come nel caso della Uster Mode). Non gestisce la Uster Mode: quella viene impostata a monte, direttamente nel loop principale.

**`private void handleClimbing(boolean toggleClimbBtn, boolean speedUpBtn, boolean speedDownBtn)`**
Gestisce il toggle del climbing (edge detection su `xStateBefore`) e la regolazione di `climbVelocity` (step di 0.1, range -0.9…0.9, edge detection su `lastUpDpad`/`lastDownDpad`). Se `climbingMode` e' vero, imposta entrambi i motori di climbing a `climbVelocity`; altrimenti li porta a 0. I tre parametri sono booleani gia' letti dal chiamante, non un `Gamepad` intero: questo permette di passare in modo esplicito lo stesso tasto (`driver.x`, `driver.dpad_up`, `driver.dpad_down`) sia in Claudio Mode che in God Mode senza duplicare la logica.

**`private void handleSlider(boolean sliderBtn)`**
Gestisce il toggle dello slider (edge detection su `sliderStateBefore`, stato in `isSliderOpen`). Se `isSliderOpen` e' vero, porta entrambi i servo slider a `SOUT`; altrimenti li porta a `SIN`. Il parametro `sliderBtn` e' il singolo tasto da usare come toggle (`operator.x` in Claudio Mode, `operator.touchpad` in God Mode). Nota: il metodo attualmente non ha Javadoc nel codice sorgente (vedi 8.5); il comportamento descritto qui e' quello osservato leggendo l'implementazione.

**`private void handleMechanisms(boolean shootBtn, boolean servoToggleBtn, boolean idleToggleBtn, boolean fullSpeedToggleBtn, boolean intakeBtn, boolean outtakeBtn)`**
E' il metodo piu' complesso: gestisce in un'unica chiamata il toggle dei servo, la logica di sparo/intake/outtake, il calcolo di `velocityok` e i due toggle del flywheel (Idle e Full Speed). L'ordine interno delle operazioni e':
1. Toggle servo (`servoToggleOpen`, edge detection su `bStateBeforeServo`).
2. Calcolo di `velocityok`: si azzera se `flywheelFullSpeed` e' falso, diventa vero se entrambi i flywheel superano 1750 RPM con `flywheelFullSpeed` vero.
3. Blocco `if/else if` che decide cosa fare tra sparo, intake, outtake (due varianti) e stato di riposo — solo uno di questi rami viene eseguito per ciclo, in base a quale condizione e' vera per prima (vedi sezione 4.2 per i dettagli dei valori).
4. Toggle Idle (`flywheelActivate`, edge detection su `xStateBeforeG2`).
5. Toggle Full Speed (`flywheelFullSpeed`, edge detection su `yStateBefore`).
6. Applicazione dello stato scelto al flywheel, con priorita' Full Speed > Idle > spento, usando `update()` per la rampa quando Full Speed e' attivo.

I sei parametri booleani vanno passati gia' letti dal chiamante (non un `Gamepad` intero) perche' Claudio Mode e God Mode usano tasti diversi per Idle e Full Speed, per evitare conflitti con altri comandi sullo stesso gamepad in God Mode.

**`public int update(int targetPower)`**
Rampa `currentPower` (campo di classe) verso `targetPower` di al massimo `maxStep` (16, costante locale al metodo) unita' per chiamata, per evitare sbalzi di corrente bruschi. Va chiamato una volta per ciclo quando il Full Speed e' attivo; il valore di ritorno va poi passato a `setVelocity()` su entrambi i flywheel. Se chiamato piu' volte nello stesso ciclo con lo stesso target, la rampa avanza piu' del previsto: va chiamato una sola volta per ciclo.

### 8.3 Inventario variabili di stato principali

| Campo | Sistema | Significato |
|---|---|---|
| `fullController`, `masterIsGamepad1` | Combo / God Mode | Se il God Mode e' attivo e quale gamepad e' master |
| `usterMode` | Guida | Se la guida Uster (tank) e' attiva, solo Claudio Mode |
| `scale` | Marce | Fattore di scala applicato alla potenza di trazione |
| `climbingMode`, `climbVelocity` | Climbing | Se il climbing e' attivo e a che velocita' |
| `isSliderOpen` | Slider | Stato del toggle slider (true = SOUT, false = SIN) |
| `servoToggleOpen` | Servo | Stato di riposo dei servo servitori fuori dallo sparo |
| `flywheelActivate` | Flywheel | Stato del toggle Idle |
| `flywheelFullSpeed` | Flywheel | Stato del toggle Full Speed |
| `velocityok` | Flywheel / sparo | Se lo sparo e' abilitato |
| `currentPower` | Flywheel | Potenza corrente usata dalla rampa in `update()` |
| Campi `*StateBefore`, `last*` | Tutti | Stato del tasto al ciclo precedente, usato per l'edge detection dei toggle |

Il campo `sliderPosition` e' ancora dichiarato e inizializzato a `SIN`, ma non viene piu' letto ne' mostrato in telemetria: e' codice morto, puo' essere rimosso in una prossima pulizia.

### 8.4 Come estendere il programma

- **Aggiungere un nuovo meccanismo condiviso tra le due modalita':** creare un nuovo metodo privato che riceve gia' i tasti come parametri booleani (seguendo lo schema di `handleClimbing`/`handleSlider`), poi chiamarlo una volta sola nel blocco `if (fullController) { ... } else { ... }`, passando i tasti giusti per ciascuna modalita'.
- **Rimappare un tasto:** basta cambiare quale campo del `Gamepad` viene passato nella chiamata al metodo condiviso, dentro `if (fullController)` o nel ramo `else`. Non serve toccare la logica interna dei metodi.
- **Aggiungere una nuova modalita' di controllo:** seguire il pattern di `driver`/`operator`: aggiungere una nuova condizione che seleziona quali `Gamepad` usare, poi richiamare gli stessi metodi condivisi con i tasti opportuni.
- **Attenzione all'ordine delle chiamate:** `driveArcade` deve precedere `applyGearAndDrive` (quest'ultimo legge `leftPower`/`rightPower`); il calcolo di `velocityok` dentro `handleMechanisms` deve precedere il blocco sparo/intake/outtake, che lo legge.

### 8.5 Incongruenze di documentazione nel codice sorgente

Elenco delle discrepanze trovate tra i commenti presenti nel codice e il comportamento reale, utile per chi apporta modifiche e non vuole fidarsi ciecamente dei commenti:

- Il commento sul campo `flywheelActivate` e il Javadoc del parametro `idleToggleBtn` in `handleMechanisms` citano ancora "X in Claudio Mode, dpad_left in God Mode": nel codice attuale il tasto Idle e' invece `operator.touchpad` in Claudio Mode e `operator.right_stick_button` in God Mode.
- Il commento sul campo `velocityok` e la spiegazione nel Javadoc di `handleMechanisms` citano ancora la soglia "1700 RPM": nel codice la soglia effettiva e' 1750 RPM.
- Il Javadoc del metodo `handleSlider` e' vuoto (`/** */`): non descrive piu' ne' il vecchio comportamento a trigger analogici ne' quello attuale a toggle. Andrebbe riscritto.
- Il blocco di commenti ASCII sopra la classe (mappa "GAMEPAD 1 | GUIDA" e "GAMEPAD 2 | MECCANISMI") descrive una mappatura piu' vecchia (es. Idle su dpad_left, slider a trigger analogici, outtake che spinge sempre il flywheel a -500): non corrisponde piu' alla mappatura reale descritta nella sezione 3 di questo README. Da aggiornare o rimuovere per evitare confusione futura.
- Il commento `//bisogna rendere una variabile la velocita di check` (dentro `handleMechanisms`, subito dopo il calcolo di `velocityok`) e' una nota promemoria del programmatore: segnala l'intenzione di rendere la soglia 1750 una costante nominata invece di un valore scritto direttamente nel codice. Non ancora fatto.

### 8.6 Avvertenze tecniche generali

- Tutti i toggle nel programma seguono lo stesso pattern: un campo booleano di stato, un campo `*StateBefore` che memorizza il valore del tasto al ciclo precedente, e un `if (tastoAttuale && !statoPrecedente)` per rilevare il fronte di salita. Se si aggiunge un nuovo toggle, seguire lo stesso pattern per evitare che scatti piu' volte mentre il tasto resta premuto.
- La priorita' tra i tre stati del flywheel (Full Speed > Idle > spento) e' implementata come una catena di `if/else if` all'interno di `handleMechanisms`: l'ordine in cui sono scritte le condizioni e' significativo, non va invertito senza capirne le conseguenze.
- Il blocco `if/else if` sparo/intake/outtake in `handleMechanisms` esegue **un solo ramo per ciclo**: se in futuro serve che due comportamenti avvengano insieme (es. sparo e outtake), la struttura va ripensata, non basta aggiungere una condizione.
- `update()` modifica uno stato interno (`currentPower`) condiviso tra i due flywheel (sinistro e destro ricevono lo stesso valore restituito): non e' pensato per rampe indipendenti sui due lati.

---
---

# ENGLISH

## 1. Project Overview

This FTC TeleOp OpMode implements two control modes: Claudio Mode (two players, default) and God Mode (one gamepad takes full control). You switch between them on the fly with the L3+R3 combo, pressable from either gamepad.

The robot handles: driving (Arcade or Uster), stepped gear shifting, a dual-motor intake/outtake system, a dual-speed flywheel with a safety check before shooting, two "servitore" servos for releasing balls, a two-position slider, an adjustable-speed climbing system, and a (currently non-functional) attempt at sound feedback.

---

## 2. Hardware/Software Changelog

Full changelog, in chronological order, reconstructed from the comments at the top of the source file. Each version is cumulative on top of the previous one.

### 9/3/26

**SW:** added independent open-servo control; added slowintake motor rotation during intake; added flywheel reverse rotation during outtake; modified idle-velocity usage (still in the code but no longer needed).
**HW:** changed the height of the first intake roller; added two pieces to block/jam balls both on the central top hole and the side holes of the flywheel; first slider prototype test.

### 9/4/26

**SW:** added a single check before shooting at 1700 rpm; implemented all functions in both God Mode and Claudio Mode; modified outtake/flywheel shooting speeds; implemented the slider servos (slider_left, slider_right) and their handle; implemented SOUT 0.22 and SIN 0.00 positions. Refactor: mechanism/climbing/driving logic (near-identical duplicated code between the two modes) was moved into unique shared methods (`handleMechanisms`, `handleClimbing`, `driveArcade`, `applyGearAndDrive`), called by both modes with the right buttons passed in — no runtime behavior change, only code organization.
**HW:** added a small polycarbonate piece under the intake motor to prevent ball jamming; the anti-rebound wall spacer pieces were moved to maximum size to try to contain balls under the basket, then moved back; physical slider implementation.

### 9/7/26

**SW:** no changes.
**HW:** raised rollers and motors to avoid ball jamming during outtake.

### 9/9/26 (first update)

**HW:** historic revisit of the intake, with the slider added and other minor tweaks.

### 9/9/26 (second update)

**SW:** made all `handle...` functions fully parametric, for controller remapping; attempted a sound implementation (not working, see the SoundPlayer section).
**HW:** adjusted flywheel and intake heights; modified the ramp adding a descent; created a net above the intake.

### 9/12/26

**SW:** added `handleSlider` with position control via the operator's analog triggers: R2 increments the position toward SOUT, L2 decrements it toward SIN, no trigger held = position locked at the last value. Added real-time slider position telemetry. *(This analog behavior was replaced the following day, see 9/13.)*
**HW:** slider finished but not working together with the intake; attempted reverse-direction shooting with the old drivetrain.

### 9/13/26

**SW:** added a flywheel safety check tied to outtake (see the Flywheel section); modified SIN and SOUT for the new servo lengths and the slider went back to being a toggle (removed the analog-trigger auxiliary function introduced the day before); all functions were moved inside `if (fullController)` to give God Mode and Claudio Mode two distinct, readable control maps.
**HW:** added a front plexiglass panel for partial intake closure even when open (net still missing); lowered one of the 4 ramp pieces for better climbing; redid the zero position of both slider servos.

### Changes found in the latest code reading, not yet noted in the changelog comments

Comparing the latest source with the previous one revealed a few functional differences not described in the comments at the top of the file. These matter because they change the robot's actual behavior:

- **"Safe" outtake with flywheel stopped:** `upIntakeMotor` power changed from 0.4 to **0.9**, `upIntakeSlowMotor` power from 0.7 to **0.2**, and the flywheel reverse velocity from -600 to **-400**.
- **Outtake with flywheel spinning:** previously only `upIntakeSlowMotor` ran at 0.9; now `upIntakeMotor` runs at 0.9 instead — it's a different motor doing the work, not just a power change. See section 4.2 for details.
- **"Slider position" telemetry:** removed. It used to show a static value that was never updated (a bug); now the line is simply gone.
- **`handleSlider` Javadoc:** the comment describing the analog-trigger control was emptied out; the method now has no documentation in the code (see section 8.5).

### Summary of value changes vs previously documented behavior

| Item | Before (previously documented) | Current |
|---|---|---|
| `velocityok` threshold | > 1700 RPM (in comments) | 1750 RPM (in code, unchanged for a while) |
| Slider | toggle (X / touchpad) | toggle (X / touchpad) — unchanged, after a brief analog-trigger detour on 9/12 that was later reverted |
| Safe outtake (flywheel stopped) | intake 0.4 / slow intake 0.7 / flywheel -600 | intake 0.9 / slow intake 0.2 / flywheel -400 |
| Outtake with flywheel spinning | ran upIntakeSlowMotor at 0.9 | runs upIntakeMotor at 0.9 |
| Slider telemetry | showed a static, never-updated value | removed |
| Init screen | — | "Jolly Roger" removed (method present but no longer called), message simplified |
| SoundPlayer | not mentioned | import commented out, failed attempt |

---

## 3. Controls Mapping

### Claudio Mode — Gamepad 1 (Driver)

| Control | Function |
|---|---|
| Left stick (Y) | Forward/back (Arcade) or left motor (Uster) |
| Right stick | Steering (Arcade) or right motor (Uster) |
| B (circle) | Toggle Uster Mode / Arcade Mode |
| X (square) | Toggle Climbing Mode |
| D-pad up / down | Increase / decrease climb speed (±0.1, range -0.9…0.9) |
| L2 / R2 | Gear down / up (power scale, step 0.25, range 0.25…1.0) |
| L3+R3 | Activate God Mode (master = gamepad1) |

### Claudio Mode — Gamepad 2 (Operator)

| Control | Function |
|---|---|
| A (cross) | Shoot: moves servos to SERVO_SHOOT and runs intake at -0.8 (only if `velocityok` is true) |
| B (circle) | Toggle servo open/closed |
| X (square) | Toggle slider open/closed |
| Y (triangle) | Toggle flywheel Full Speed (2000 RPM) |
| Touchpad | Toggle flywheel Idle (900 RPM) |
| R1 | Intake |
| L1 | "Safe" outtake (see section 4.2) |
| L3+R3 | Activate God Mode (master = gamepad2) |

### God Mode (master gamepad, activated with L3+R3)

A single gamepad controls both driving and mechanisms. The layout mirrors Gamepad 1 for driving, but mechanisms are remapped to avoid conflicting with climbing (which stays on X and D-pad up/down).

| Control | Function |
|---|---|
| Left/right stick | Arcade drive (Uster Mode unavailable) |
| A | Shoot (requires `velocityok`) |
| B | Toggle servo open/closed |
| X | Toggle Climbing Mode |
| Y | Toggle flywheel Full Speed |
| R3 (right stick button) | Toggle flywheel Idle |
| Touchpad | Toggle slider open/closed |
| D-pad up / down | Adjust climb speed |
| R1 | Intake |
| L1 | "Safe" outtake |
| L2 / R2 | Gear down / up |
| L3+R3 | Deactivate God Mode |

---

## 4. Core Systems

### 4.1 Driving: Arcade vs Uster

The Arcade Drive (`driveArcade`) computes `leftPower`/`rightPower` from throttle + steering, normalizing if the sum exceeds 1.0. It's the only mode available in God Mode. Uster Mode (tank-style dual stick, one motor per stick) exists only in Claudio Mode and is toggled with B on gamepad1. In both cases, `applyGearAndDrive` then applies the gear (`scale`) and sends power to the motors.

### 4.2 Dual-speed flywheel and shooting safety

Three possible states, in priority order:

1. **Full Speed** (if active): ramps up gradually to `TARGET_VELOCITY` (2000) via the `update()` method, max step 16 per cycle, to avoid current spikes.
2. **Idle** (if active and Full Speed off): fixed at `IDLE_VELOCITY` (900), no ramping.
3. **Off**: 0, unless an outtake is in progress with the flywheel stopped (see below).

The `velocityok` variable enables shooting (A button): it becomes true only when both flywheels exceed 1750 RPM with Full Speed active, and stays true until Full Speed is turned off (resets immediately when it is). Shooting no longer checks instantaneous velocity at the moment A is pressed, but this variable, recomputed every cycle. (Note: some comments in the code still refer to the old 1700 RPM threshold — see section 8.5.)

**"Safe" outtake:** holding L1, behavior depends on flywheel state:

- Flywheel **stopped** (Idle and Full Speed both off, velocity < 50 RPM on both motors): `upIntakeMotor` runs at **0.9**, `upIntakeSlowMotor` at **0.2**, and the flywheel is pushed to **-400** on both sides to help clear jammed balls.
- Flywheel **spinning** (Idle or Full Speed active, or velocity > 50 RPM on either motor): only `upIntakeMotor` runs, at 0.9, without touching the flywheel or `upIntakeSlowMotor` — this prevents shooting balls the wrong way if outtake is pressed by mistake while the flywheel is up to speed.

### 4.3 Slider

Two servos (`slider_dx` → `sliderRight`, `slider_sx` → `sliderLeft`, the latter reversed) controlled as a toggle between two fixed positions: `SIN` (in, 0.86) and `SOUT` (out, 0.0). Toggled with X on gamepad2 in Claudio Mode, or the master's touchpad in God Mode.

Historical note: on 9/12 an analog control was tried (R2 to open, L2 to close, with a free intermediate position), then abandoned the next day in favor of the two-position toggle. The `handleSlider` Javadoc that described that behavior has since been emptied out (see section 8.5): the method currently has no comments in the source, but the real behavior is still the toggle described here.

### 4.4 Climbing

Toggle activated with X (same button on the driver in both modes, shared via `handleClimbing`). When active, both climbing motors (`climb_motor_int`, `climb_motor_est`) run at `climbVelocity` (default 0.5, adjustable with D-pad up/down in 0.1 steps, range -0.9…0.9). When off, power is 0.

### 4.5 Servos (servitori)

`servitoreRight` and `servitoreLeft` are the servos that hold/release balls. They have three possible positions managed by constants: `SERVO_CLOSE` (0.0), `SERVO_OPEN` (0.2, used during intake/outtake depending on the toggle) and `SERVO_SHOOT` (0.2, used only while shooting). The open/closed toggle (B) determines which of the two "resting" positions is used outside of shooting.

### 4.6 SoundPlayer (experimental, not working)

On 9/9 an attempt was made to use `com.qualcomm.ftccommon.SoundPlayer` to add audio feedback (e.g. to confirm shooting or a mode switch), but the attempt didn't work: in the current code the import is commented out (`//import com.qualcomm.ftccommon.SoundPlayer;`) and there is no active call to the audio system. The robot currently produces no sound feedback; this remains to be picked up in a future iteration.

---

## 5. Main Constants

| Constant | Value | Notes |
|---|---|---|
| `TARGET_VELOCITY` | 2000 | Flywheel Full Speed |
| `IDLE_VELOCITY` | 900 | Flywheel Idle |
| `SERVO_CLOSE` | 0.0 | Closed servo position |
| `SERVO_OPEN` | 0.2 | Open servo position (intake/outtake) |
| `SERVO_SHOOT` | 0.2 | Servo position while shooting |
| `SIN` | 0.86 | Slider in |
| `SOUT` | 0.0 | Slider out |
| `scale` (initial) | 0.75 | Starting gear, range 0.25–1.0 |
| `climbVelocity` (initial) | 0.5 | Range -0.9…0.9 |
| `maxStep` (in `update()`) | 16 | Max flywheel ramp per cycle |
| `velocityok` threshold | 1750 RPM | On both flywheels, with Full Speed active |
| Safe-outtake threshold | 50 RPM | On both flywheels, to enable the reverse |
| Outtake reverse velocity | -400 | Not a named constant, it's a value hardcoded directly in the method (magic number) |

---

## 6. Required Hardware (configuration names)

`left_motor`, `right_motor`, `intake_motor`, `second_intake_motor`, `flywheel_left`, `flywheel_right` (DcMotorEx with encoder), `servitore_1` (→ servitoreRight, REVERSE), `servitore_2` (→ servitoreLeft, FORWARD), `slider_dx` (→ sliderRight, FORWARD), `slider_sx` (→ sliderLeft, REVERSE), `climb_motor_int`, `climb_motor_est`.

---

## 7. General Notes

- All toggles (Uster, Climbing, Flywheel Idle, Flywheel Full Speed, Servo, Slider, God Mode) use standard edge detection: they fire once per press, no flickering if the button stays held.
- Uster Mode is intentionally excluded from God Mode; driving is Arcade-only there.
- The init screen was simplified: the previously shown "Jolly Roger" was removed from the call (the method is still in the code but is no longer invoked).
- The SoundPlayer system is present only as an attempt, with the import disabled: no sound is currently played by the robot.

---

## 8. Technical Code Documentation

This section describes how the program is organized and how to use/extend each function, for anyone working on the code.

### 8.1 General Architecture

The program is a single `LinearOpMode` with one main loop (`while (opModeIsActive())`). Each loop iteration:

1. Reads the L3+R3 combo on both gamepads to toggle `fullController` (God Mode) and determine `masterIsGamepad1`.
2. Computes three `Gamepad` references: `master` (the gamepad that activated God Mode), `driver` (who drives this cycle) and `operator` (who controls mechanisms this cycle). In Claudio Mode `driver = gamepad1` and `operator = gamepad2`; in God Mode both point to `master`.
3. Handles driving (Arcade or Uster).
4. Inside the `if (fullController) { ... } else { ... }` block, calls the shared methods — `applyGearAndDrive`, `handleClimbing`, `handleSlider`, `handleMechanisms` — with the right buttons for the active mode.
5. Updates telemetry.

The key architectural point is that **the logic lives only once**, in the shared methods described below; what changes between Claudio Mode and God Mode is only *which buttons* get passed in as parameters. If a mechanism's behavior needs to change, edit the shared method; if only which button triggers it needs to change, edit the call inside the `if (fullController) / else` block.

### 8.2 Method Reference

**`private void driveArcade(double throttle, double spin)`**
Computes `leftPower` and `rightPower` for Arcade driving by adding/subtracting `throttle` and `spin`, then normalizes if the sum of absolute values exceeds 1.0. Writes directly to the class fields `leftPower`/`rightPower`, returns nothing. Must be called before `applyGearAndDrive`, since the latter reads those two fields. Typical usage: `driveArcade(-driver.left_stick_y, driver.right_stick_x)`.

**`private void applyGearAndDrive(boolean shiftUpBtn, boolean shiftDownBtn)`**
Handles gear shifting: if `shiftUpBtn` goes from false to true (edge detection via `lastRightTrigger`), increases `scale` by 0.25 up to a maximum of 1.0; if `shiftDownBtn` goes from false to true (via `lastLeftTrigger`), decreases `scale` by 0.25 down to a minimum of 0.25. Then applies `scale` to `leftPower`/`rightPower` and sends them with `setPower()` to the drive motors. Must be called after `driveArcade` (or after manually setting `leftPower`/`rightPower`, as with Uster Mode). Does not handle Uster Mode: that's set upstream, directly in the main loop.

**`private void handleClimbing(boolean toggleClimbBtn, boolean speedUpBtn, boolean speedDownBtn)`**
Handles the climbing toggle (edge detection on `xStateBefore`) and adjustment of `climbVelocity` (0.1 steps, range -0.9…0.9, edge detection on `lastUpDpad`/`lastDownDpad`). If `climbingMode` is true, sets both climbing motors to `climbVelocity`; otherwise sets them to 0. The three parameters are booleans already read by the caller, not a whole `Gamepad`: this allows explicitly passing the same button (`driver.x`, `driver.dpad_up`, `driver.dpad_down`) in both Claudio Mode and God Mode without duplicating the logic.

**`private void handleSlider(boolean sliderBtn)`**
Handles the slider toggle (edge detection on `sliderStateBefore`, state in `isSliderOpen`). If `isSliderOpen` is true, moves both slider servos to `SOUT`; otherwise to `SIN`. The `sliderBtn` parameter is the single button used as the toggle (`operator.x` in Claudio Mode, `operator.touchpad` in God Mode). Note: the method currently has no Javadoc in the source (see 8.5); the behavior described here is what's observed by reading the implementation.

**`private void handleMechanisms(boolean shootBtn, boolean servoToggleBtn, boolean idleToggleBtn, boolean fullSpeedToggleBtn, boolean intakeBtn, boolean outtakeBtn)`**
This is the most complex method: it handles, in a single call, the servo toggle, the shoot/intake/outtake logic, the `velocityok` calculation, and the two flywheel toggles (Idle and Full Speed). Internal order of operations:
1. Servo toggle (`servoToggleOpen`, edge detection on `bStateBeforeServo`).
2. `velocityok` calculation: resets to false if `flywheelFullSpeed` is false, becomes true if both flywheels exceed 1750 RPM with `flywheelFullSpeed` true.
3. An `if/else if` block deciding between shoot, intake, outtake (two variants), and resting state — only one branch runs per cycle, based on which condition is true first (see section 4.2 for the value details).
4. Idle toggle (`flywheelActivate`, edge detection on `xStateBeforeG2`).
5. Full Speed toggle (`flywheelFullSpeed`, edge detection on `yStateBefore`).
6. Applying the chosen state to the flywheel, priority Full Speed > Idle > off, using `update()` for the ramp when Full Speed is active.

The six boolean parameters must be passed already read by the caller (not a whole `Gamepad`) because Claudio Mode and God Mode use different buttons for Idle and Full Speed, to avoid conflicts with other commands on the same gamepad in God Mode.

**`public int update(int targetPower)`**
Ramps `currentPower` (a class field) toward `targetPower` by at most `maxStep` (16, a local constant in the method) units per call, to avoid abrupt current spikes. Must be called once per cycle when Full Speed is active; the returned value is then passed to `setVelocity()` on both flywheels. If called more than once per cycle with the same target, the ramp advances further than intended: call it only once per cycle.

### 8.3 Main State Variable Inventory

| Field | System | Meaning |
|---|---|---|
| `fullController`, `masterIsGamepad1` | Combo / God Mode | Whether God Mode is active and which gamepad is master |
| `usterMode` | Driving | Whether Uster (tank) driving is active, Claudio Mode only |
| `scale` | Gearing | Scale factor applied to drive power |
| `climbingMode`, `climbVelocity` | Climbing | Whether climbing is active and at what speed |
| `isSliderOpen` | Slider | Slider toggle state (true = SOUT, false = SIN) |
| `servoToggleOpen` | Servos | Resting state of the servitore servos outside of shooting |
| `flywheelActivate` | Flywheel | Idle toggle state |
| `flywheelFullSpeed` | Flywheel | Full Speed toggle state |
| `velocityok` | Flywheel / shooting | Whether shooting is enabled |
| `currentPower` | Flywheel | Current power used by the ramp in `update()` |
| `*StateBefore`, `last*` fields | All | Button state from the previous cycle, used for toggle edge detection |

The `sliderPosition` field is still declared and initialized to `SIN`, but is no longer read or shown in telemetry: it's dead code and can be removed in a future cleanup.

### 8.4 How to Extend the Program

- **Adding a new mechanism shared between both modes:** create a new private method that already receives buttons as boolean parameters (following the `handleClimbing`/`handleSlider` pattern), then call it once inside the `if (fullController) { ... } else { ... }` block, passing the right buttons for each mode.
- **Remapping a button:** just change which `Gamepad` field gets passed in the call to the shared method, inside `if (fullController)` or the `else` branch. No need to touch the method's internal logic.
- **Adding a new control mode:** follow the `driver`/`operator` pattern: add a new condition that selects which `Gamepad`s to use, then call the same shared methods with the appropriate buttons.
- **Watch the call order:** `driveArcade` must come before `applyGearAndDrive` (the latter reads `leftPower`/`rightPower`); the `velocityok` calculation inside `handleMechanisms` must come before the shoot/intake/outtake block, which reads it.

### 8.5 Documentation Inconsistencies in the Source Code

List of discrepancies found between the comments in the code and its actual behavior, useful for anyone making changes who shouldn't blindly trust the comments:

- The comment on the `flywheelActivate` field and the Javadoc for the `idleToggleBtn` parameter in `handleMechanisms` still say "X in Claudio Mode, dpad_left in God Mode": in the current code the Idle button is actually `operator.touchpad` in Claudio Mode and `operator.right_stick_button` in God Mode.
- The comment on the `velocityok` field and the explanation in `handleMechanisms`' Javadoc still cite the "1700 RPM" threshold: the actual threshold in the code is 1750 RPM.
- The `handleSlider` method's Javadoc is empty (`/** */`): it no longer describes either the old analog-trigger behavior or the current toggle. It should be rewritten.
- The ASCII comment block above the class (the "GAMEPAD 1 | GUIDA" and "GAMEPAD 2 | MECCANISMI" maps) describes an older mapping (e.g. Idle on dpad_left, slider on analog triggers, outtake always pushing the flywheel to -500): it no longer matches the real mapping described in section 3 of this README. It should be updated or removed to avoid future confusion.
- The comment `//bisogna rendere una variabile la velocita di check` ("this check speed should become a variable", inside `handleMechanisms`, right after the `velocityok` calculation) is a programmer's reminder note: it flags the intent to turn the 1750 threshold into a named constant instead of a hardcoded value. Not done yet.

### 8.6 General Technical Warnings

- Every toggle in the program follows the same pattern: a boolean state field, a `*StateBefore` field storing the button's value from the previous cycle, and an `if (currentButton && !previousState)` to detect the rising edge. If adding a new toggle, follow the same pattern to avoid it firing repeatedly while the button is held.
- The priority among the three flywheel states (Full Speed > Idle > off) is implemented as a chain of `if/else if` inside `handleMechanisms`: the order in which the conditions are written matters and shouldn't be swapped without understanding the consequences.
- The shoot/intake/outtake `if/else if` block in `handleMechanisms` runs **only one branch per cycle**: if two behaviors ever need to happen together (e.g. shooting and outtake), the structure needs to be rethought, not just given an extra condition.
- `update()` modifies a single internal state (`currentPower`) shared between both flywheels (left and right both receive the same returned value): it's not designed for independent ramps on the two sides.