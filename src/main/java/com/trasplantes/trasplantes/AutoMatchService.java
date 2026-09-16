package com.trasplantes.trasplantes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Servicio principal de matching automatico del sistema
 * Organ Transplant AutoMatch System.
 *
 * Aplica dos estructuras de datos clave:
 * 1. Tabla Hash (HashMap) para verificar compatibilidad sanguinea ABO/Rh en O(1).
 * 2. Cola de Prioridad (PriorityQueue / Max-Heap) para ordenar dinamicamente
 *    a los receptores compatibles segun la puntuacion de prioridad clinica
 *    correspondiente al tipo de organo (MELD, urgencia cardiaca, o HLA renal).
 */
public class AutoMatchService {

    // ------------------------------------------------------------------
    // Estructura 1: Tabla Hash de compatibilidad sanguinea ABO/Rh
    // clave: tipo de sangre del DONANTE -> valor: lista de tipos de sangre
    // de RECEPTORES compatibles (verificacion en O(1) con contains sobre
    // una estructura hash interna).
    // ------------------------------------------------------------------
    private final Map<String, List<String>> compatibilidadSanguinea;

    public AutoMatchService() {
        this.compatibilidadSanguinea = construirTablaCompatibilidad();
    }

    /**
     * Construye la tabla hash de compatibilidad ABO/Rh.
     * O+ puede donar a: O+, A+, B+, AB+
     * O- puede donar a: O-, O+, A-, A+, B-, B+, AB-, AB+ (donante universal)
     * A+ puede donar a: A+, AB+
     * A- puede donar a: A-, A+, AB-, AB+
     * B+ puede donar a: B+, AB+
     * B- puede donar a: B-, B+, AB-, AB+
     * AB+ puede donar a: AB+ (receptor universal, no donante universal)
     * AB- puede donar a: AB-, AB+
     */
    private Map<String, List<String>> construirTablaCompatibilidad() {
        Map<String, List<String>> tabla = new HashMap<>();

        tabla.put("O-",  List.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"));
        tabla.put("O+",  List.of("O+", "A+", "B+", "AB+"));
        tabla.put("A-",  List.of("A-", "A+", "AB-", "AB+"));
        tabla.put("A+",  List.of("A+", "AB+"));
        tabla.put("B-",  List.of("B-", "B+", "AB-", "AB+"));
        tabla.put("B+",  List.of("B+", "AB+"));
        tabla.put("AB-", List.of("AB-", "AB+"));
        tabla.put("AB+", List.of("AB+"));

        return tabla;
    }

    /**
     * Verifica en O(1) si el tipo de sangre del donante es compatible
     * con el tipo de sangre del receptor, usando la tabla hash precalculada.
     *
     * @param tipoSangreDonante  tipo de sangre del organo/donante
     * @param tipoSangreReceptor tipo de sangre del paciente receptor
     * @return true si es compatible, false en caso contrario
     */
    public boolean esCompatibleSangre(String tipoSangreDonante, String tipoSangreReceptor) {
        List<String> receptoresCompatibles = compatibilidadSanguinea.get(tipoSangreDonante);
        if (receptoresCompatibles == null) {
            return false;
        }
        return receptoresCompatibles.contains(tipoSangreReceptor);
    }

    // ------------------------------------------------------------------
    // Metodo principal: ejecutarMatch
    // ------------------------------------------------------------------

    /**
     * Ejecuta el proceso de matching automatico para un organo disponible:
     * 1. Obtiene todos los pacientes activos que requieren ese tipo de organo.
     * 2. Filtra en O(1) por compatibilidad sanguinea usando la tabla hash.
     * 3. Calcula la puntuacion de prioridad clinica de cada paciente compatible
     *    segun el tipo de organo (MELD, urgencia cardiaca o HLA renal).
     * 4. Inserta a los candidatos en un Max-Heap (PriorityQueue) ordenado
     *    por puntuacion descendente.
     * 5. Extrae el paciente con mayor prioridad (raiz del heap) y construye
     *    el MatchResult con el desglose de su puntuacion.
     *
     * @param organo organo disponible a asignar
     * @return MatchResult con el receptor de mayor prioridad, o un MatchResult
     *         vacio (sin paciente) si no hay candidatos compatibles
     * @throws SQLException si ocurre un error de acceso a la base de datos
     */
    public MatchResult ejecutarMatch(Organo organo) throws SQLException {

        List<Paciente> candidatos = obtenerPacientesPorOrgano(organo.getTipoOrgano());

        // Comparator para Max-Heap: mayor puntuacion tiene mayor prioridad
        Comparator<Paciente> comparadorPorPuntuacion = (p1, p2) ->
                Double.compare(
                        calcularPuntuacion(p2, organo),
                        calcularPuntuacion(p1, organo)
                );

        PriorityQueue<Paciente> maxHeap = new PriorityQueue<>(comparadorPorPuntuacion);

        // Filtrado O(1) por compatibilidad sanguinea + insercion en el heap
        for (Paciente candidato : candidatos) {
            if (!candidato.isActivo()) {
                continue;
            }
            if (esCompatibleSangre(organo.getTipoSangre(), candidato.getTipoSangre())) {
                maxHeap.offer(candidato);
            }
        }

        if (maxHeap.isEmpty()) {
            MatchResult resultadoVacio = new MatchResult();
            resultadoVacio.setOrganoAsignado(organo);
            resultadoVacio.setPacienteGanador(null);
            resultadoVacio.setFechaMatch(LocalDateTime.now());
            resultadoVacio.setPuntuacionTotal(0.0);
            resultadoVacio.agregarCriterio("mensaje", -1.0);
            return resultadoVacio;
        }

        Paciente ganador = maxHeap.poll();

        MatchResult resultado = new MatchResult();
        resultado.setPacienteGanador(ganador);
        resultado.setOrganoAsignado(organo);
        resultado.setFechaMatch(LocalDateTime.now());

        Map<String, Double> desglose = construirDesglosePuntuacion(ganador, organo);
        resultado.setDesglosePuntuacion(desglose);
        resultado.setPuntuacionTotal(calcularPuntuacion(ganador, organo));

        return resultado;
    }

    // ------------------------------------------------------------------
    // Calculo de puntuacion segun tipo de organo
    // ------------------------------------------------------------------

    /**
     * Calcula la puntuacion total de prioridad de un paciente para un organo dado,
     * segun el tipo de organo requerido.
     */
    private double calcularPuntuacion(Paciente paciente, Organo organo) {
        switch (organo.getTipoOrgano()) {
            case Paciente.ORGANO_HIGADO:
                return calcularScoreMELD(paciente);
            case Paciente.ORGANO_CORAZON:
                return calcularScoreCorazon(paciente, organo);
            case Paciente.ORGANO_RINON:
                return calcularScoreRinon(paciente, organo);
            default:
                return 0.0;
        }
    }

    /**
     * Calcula el score MELD (Model for End-Stage Liver Disease) de un paciente.
     * Formula estandar:
     * MELD = 3.78*ln(bilirrubina) + 11.2*ln(INR) + 9.57*ln(creatinina) + 6.43
     *
     * Se aplican los limites clinicos estandar: valores minimos de 1.0 para evitar
     * logaritmos de cero o negativos, y creatinina maxima de 4.0.
     */
    public double calcularScoreMELD(Paciente paciente) {
        if (paciente.getBilirrubina() == null || paciente.getCreatinina() == null
                || paciente.getInr() == null) {
            return 0.0;
        }

        double bilirrubina = Math.max(paciente.getBilirrubina(), 1.0);
        double inr = Math.max(paciente.getInr(), 1.0);
        double creatinina = Math.min(Math.max(paciente.getCreatinina(), 1.0), 4.0);

        double meld = 3.78 * Math.log(bilirrubina)
                + 11.2 * Math.log(inr)
                + 9.57 * Math.log(creatinina)
                + 6.43;

        // El score MELD se redondea y se limita entre 6 y 40 segun estandar clinico
        meld = Math.round(meld * 100.0) / 100.0;
        meld = Math.max(6.0, Math.min(meld, 40.0));

        return meld;
    }

    /**
     * Calcula el score de prioridad para trasplante de corazon:
     * Puntuacion base por urgencia UNOS (1A, 1B, 2) + bonificacion
     * por compatibilidad sanguinea exacta (mismo tipo exacto donante/receptor)
     * + bonificacion menor por tiempo de espera.
     */
    public double calcularScoreCorazon(Paciente paciente, Organo organo) {
        double scoreUrgencia;
        String urgencia = paciente.getUrgenciaCorazon();

        if (urgencia == null) {
            scoreUrgencia = 0.0;
        } else {
            switch (urgencia) {
                case "1A":
                    scoreUrgencia = 100.0;
                    break;
                case "1B":
                    scoreUrgencia = 70.0;
                    break;
                case "2":
                    scoreUrgencia = 40.0;
                    break;
                default:
                    scoreUrgencia = 0.0;
            }
        }

        // Bonificacion por compatibilidad sanguinea exacta (idealmente reduce
        // la carga inmunologica frente a una compatibilidad ABO amplia)
        double bonificacionCompatibilidadExacta = 0.0;
        if (organo.getTipoSangre() != null && organo.getTipoSangre().equals(paciente.getTipoSangre())) {
            bonificacionCompatibilidadExacta = 10.0;
        }

        // Bonificacion leve por tiempo de espera (max 10 puntos, 1 punto cada 30 dias)
        double bonificacionEspera = Math.min(paciente.getDiasEspera() / 30.0, 10.0);

        return scoreUrgencia + bonificacionCompatibilidadExacta + bonificacionEspera;
    }

    /**
     * Calcula el score de prioridad para trasplante de rinon:
     * Puntuacion por alelos HLA coincidentes entre donante y receptor
     * (0 a 6 alelos, cada alelo compatible otorga 15 puntos) +
     * puntuacion por tiempo en lista de espera (1 punto cada 10 dias, max 40).
     */
    public double calcularScoreRinon(Paciente paciente, Organo organo) {
        int alelosPaciente = (paciente.getHlaAlelos() != null) ? paciente.getHlaAlelos() : 0;
        int alelosDonante = (organo.getHlaAlelosDonante() != null) ? organo.getHlaAlelosDonante() : 0;

        // Alelos coincidentes efectivos: el minimo entre lo que requiere/tiene
        // el paciente y lo que aporta el donante, limitado a 6
        int alelosCoincidentes = Math.min(alelosPaciente, alelosDonante);
        alelosCoincidentes = Math.max(0, Math.min(alelosCoincidentes, 6));

        double scoreHLA = alelosCoincidentes * 15.0;

        double scoreEspera = Math.min(paciente.getDiasEspera() / 10.0, 40.0);

        return scoreHLA + scoreEspera;
    }

    // ------------------------------------------------------------------
    // Construccion del desglose detallado de puntuacion
    // ------------------------------------------------------------------

    private Map<String, Double> construirDesglosePuntuacion(Paciente paciente, Organo organo) {
        Map<String, Double> desglose = new HashMap<>();

        switch (organo.getTipoOrgano()) {
            case Paciente.ORGANO_HIGADO:
                desglose.put("scoreMELD", calcularScoreMELD(paciente));
                desglose.put("bilirrubina", paciente.getBilirrubina() != null ? paciente.getBilirrubina() : 0.0);
                desglose.put("creatinina", paciente.getCreatinina() != null ? paciente.getCreatinina() : 0.0);
                desglose.put("inr", paciente.getInr() != null ? paciente.getInr() : 0.0);
                break;

            case Paciente.ORGANO_CORAZON:
                double scoreUrgencia;
                switch (paciente.getUrgenciaCorazon() != null ? paciente.getUrgenciaCorazon() : "") {
                    case "1A": scoreUrgencia = 100.0; break;
                    case "1B": scoreUrgencia = 70.0; break;
                    case "2":  scoreUrgencia = 40.0; break;
                    default:   scoreUrgencia = 0.0;
                }
                double bonifExacta = (organo.getTipoSangre() != null
                        && organo.getTipoSangre().equals(paciente.getTipoSangre())) ? 10.0 : 0.0;
                double bonifEspera = Math.min(paciente.getDiasEspera() / 30.0, 10.0);

                desglose.put("scoreUrgencia", scoreUrgencia);
                desglose.put("bonificacionCompatibilidadExacta", bonifExacta);
                desglose.put("bonificacionEspera", bonifEspera);
                break;

            case Paciente.ORGANO_RINON:
                int alelosPaciente = (paciente.getHlaAlelos() != null) ? paciente.getHlaAlelos() : 0;
                int alelosDonante = (organo.getHlaAlelosDonante() != null) ? organo.getHlaAlelosDonante() : 0;
                int alelosCoincidentes = Math.max(0, Math.min(Math.min(alelosPaciente, alelosDonante), 6));
                double scoreHLA = alelosCoincidentes * 15.0;
                double scoreEspera = Math.min(paciente.getDiasEspera() / 10.0, 40.0);

                desglose.put("alelosCoincidentes", (double) alelosCoincidentes);
                desglose.put("scoreHLA", scoreHLA);
                desglose.put("scoreEsperaRinon", scoreEspera);
                break;

            default:
                break;
        }

        desglose.put("diasEspera", (double) paciente.getDiasEspera());
        desglose.put("puntuacionTotal", calcularPuntuacion(paciente, organo));

        return desglose;
    }

    // ------------------------------------------------------------------
    // Acceso a datos: obtiene pacientes activos por tipo de organo requerido
    // ------------------------------------------------------------------

    /**
     * Obtiene de la base de datos todos los pacientes activos que requieren
     * el tipo de organo especificado.
     *
     * @param tipoOrgano tipo de organo requerido (HIGADO, CORAZON, RINON)
     * @return lista de pacientes candidatos (sin filtrar aun por sangre)
     * @throws SQLException si ocurre un error de acceso a la base de datos
     */
    private List<Paciente> obtenerPacientesPorOrgano(String tipoOrgano) throws SQLException {
        List<Paciente> pacientes = new ArrayList<>();

        String sql = "SELECT id_paciente, nombre, tipo_sangre, organo_requerido, fecha_inscripcion, "
                + "activo, bilirrubina, creatinina, inr, urgencia_corazon, hla_alelos, dias_espera "
                + "FROM pacientes WHERE organo_requerido = ? AND activo = 1";

        Connection con = ConexionBD.getConexion();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tipoOrgano);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Paciente p = new Paciente();
                    p.setIdPaciente(rs.getInt("id_paciente"));
                    p.setNombre(rs.getString("nombre"));
                    p.setTipoSangre(rs.getString("tipo_sangre"));
                    p.setOrganoRequerido(rs.getString("organo_requerido"));
                    p.setFechaInscripcion(rs.getString("fecha_inscripcion"));
                    p.setActivo(rs.getInt("activo") == 1);

                    double bilirrubina = rs.getDouble("bilirrubina");
                    p.setBilirrubina(rs.wasNull() ? null : bilirrubina);

                    double creatinina = rs.getDouble("creatinina");
                    p.setCreatinina(rs.wasNull() ? null : creatinina);

                    double inr = rs.getDouble("inr");
                    p.setInr(rs.wasNull() ? null : inr);

                    p.setUrgenciaCorazon(rs.getString("urgencia_corazon"));

                    int hlaAlelos = rs.getInt("hla_alelos");
                    p.setHlaAlelos(rs.wasNull() ? null : hlaAlelos);

                    p.setDiasEspera(rs.getInt("dias_espera"));

                    pacientes.add(p);
                }
            }
        }

        return pacientes;
    }

    // ------------------------------------------------------------------
    // Metodo main de prueba manual
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        AutoMatchService service = new AutoMatchService();

        try {
            Organo organoHigado = new Organo();
            organoHigado.setIdOrgano(1);
            organoHigado.setTipoOrgano(Paciente.ORGANO_HIGADO);
            organoHigado.setTipoSangre("O+");
            organoHigado.setDonanteNombre("Donante Anonimo 1");
            organoHigado.setFechaDisponible("2025-09-10");
            organoHigado.setTiempoIsquemiaMax(12);
            organoHigado.setEstado(Organo.ESTADO_DISPONIBLE);

            MatchResult resultado = service.ejecutarMatch(organoHigado);

            System.out.println("=== Resultado del Match ===");
            if (resultado.getPacienteGanador() != null) {
                System.out.println("Paciente ganador: " + resultado.getPacienteGanador().getNombre());
                System.out.println("Puntuacion total: " + resultado.getPuntuacionTotal());
                System.out.println("Desglose: " + resultado.getDesglosePuntuacion());
            } else {
                System.out.println("No se encontraron candidatos compatibles.");
            }

        } catch (SQLException e) {
            System.err.println("Error durante el proceso de matching: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion();
        }
    }
}