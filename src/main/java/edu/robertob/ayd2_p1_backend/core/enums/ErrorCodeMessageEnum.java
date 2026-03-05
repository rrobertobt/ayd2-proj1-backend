package edu.robertob.ayd2_p1_backend.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCodeMessageEnum {
        // --- Errores JWT ---
        JWT_INVALID("JWT-001", "El token JWT es inválido o ha sido manipulado"),
        JWT_NO_EXPIRATION("JWT-002", "El token JWT no contiene fecha de expiración"),
        JWT_NO_CLIENT_ID("JWT-003", "No se pudo extraer el clientId del token JWT"),
        JWT_NO_AUTHORITIES("JWT-004", "No se pudo extraer los permisos del usuario del token JWT"),
        CLAIM_TYPE_MISMATCH("JWT-005", "El valor de la claim no coincide con el tipo esperado"),
        JWT_NO_USER_TYPE("JWT-006", "El token JWT no contiene el tipo de usuario"),
        JWT_NO_USERNAME("JWT-007", "El token JWT no contiene el nombre de usuario"),
        JWT_UNSUPPORTED("JWT-008", "El token JWT tiene un formato no soportado"),
        JWT_MALFORMED("JWT-009", "El token JWT está malformado"),
        JWT_SIGNATURE_INVALID("JWT-010", "La firma del token JWT no es válida"),
        JWT_EXPIRED("JWT-011", "El token JWT ha expirado"),
        JWT_ILLEGAL_ARGUMENT("JWT-012", "El token JWT está vacío o contiene solo espacios"),

        // -- Errores de archivos
        FILE_ALREADY_EXISTS("FILE-001", "El archivo ya existe y no puede ser reemplazado"),
        DIRECTORY_NOT_EMPTY("FILE-002", "El destino de la operacion es un directorio no vacío"),
        UNSUPPORTED_FILE_OPERATION("FILE-003",
                        "La operación de copia no es soportada por el sistema de archivos actual"),
        FILE_SECURITY_EXCEPTION("FILE-004", "Permiso denegado al intentar manipular el archivo"),
        FILE_IO_EXCEPTION("FILE-005", "Error de entrada/salida al manipular el archivo"),
        FILE_EXTENSION_NULL_OR_INVALID("FILE-006", "El archivo no tiene una extensión ausente"),
        FILE_INVALID_PATH("FILE-008",
                        "El nombre del archivo contiene caracteres inválidos o genera una ruta incorrecta"),

        FILE_NOT_FOUND_FOR_DELETE("FILE-09", "El archivo a eliminar no existe en el sistema"),
        FILE_READ_OUT_OF_MEMORY("FILE-010", "El archivo es demasiado grande para ser leído completamente en memoria."),
        FILE_INPUT_STREAM_ERROR("FILE-011",
                        "No se pudo obtener el flujo de entrada del archivo cargado (posible fallo en el almacenamiento temporal del archivo)"),

        // ------------------------------------------------------ errores en QR
        NO_AVAILABLE_QR_CODE("QR-001",
                        "No se encontró ningún código QR disponible para asignar."),
        QR_ALREADY_ASSIGNED("QR-002",
                        "El código QR que se intentó asignar ya está vinculado a un participante. Es posible que haya sido utilizado por otro proceso."),
        PARTICIPANT_ALREADY_HAS_QR("QR-003",
                        "El Participante que se intentó asignar ya está vinculado a un QR. Es posible que haya sido utilizado por otro proceso."),

        QR_GENERATION_FAILED("QR-004", "Ocurrió un error al generar el código QR."),
        QR_IMAGE_ENCODING_FAILED("QR-005", "No se pudo codificar la imagen del código QR en formato PNG."),

        // ----Errores por CSV
        CSV_WRITE_FAILED("CSV-001",
                        "No se pudo generar el archivo CSV en memoria (posible fallo en el almacenamiento temporal del archivo)"),
        // ------------------------------------------------------ errores por mala
        // programacion
        REGISTRATION_STATUS_INCOMPLETE("STP-001",
                        "El 'isApproved' no puede ser nulo al crear un estado de registro."),

        NO_AUTHOR_REGISTRATION_STATUS_INCOMPLETE("STP-002",
                        "El campo 'isCashPayment' no puede ser nulo al crear un estado de registro de un participant4 no autor."),
        EMAIL_REPORT_GENERATION_FAILED("STP-003",
                        "Ocurrió un error al generar el reporte de correos electrónicos de participantes aprobados."),
        // errores por cambios de contrasenia
        PARTICIPANT_WITHOUT_USER("PAS-001",
                        "El participante seleccionado no tiene un usuario asociado. "
                                        + "No es posible cambiar la contraseña sin una cuenta vinculada."),
                                        ;

        private final String code;
        private final String message;

}
