package com.elearning.platform.controller;

import com.elearning.platform.dto.AuthDtos.*;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.AuthService;
import com.elearning.platform.services.DesbloqueoService;
import com.elearning.platform.services.RecuperacionService;
import com.elearning.platform.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** RF-002 Autenticar, RF-003 Recuperar contraseña y datos de la sesión actual. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;
    private final RecuperacionService recuperacion;
    private final DesbloqueoService desbloqueo;

    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return auth.login(req, IpUtil.de(http));
    }

    @PostMapping("/otp/verificar")
    public SesionRespuesta verificarOtp(@Valid @RequestBody OtpRequest req, HttpServletRequest http) {
        return auth.verificarOtp(req, IpUtil.de(http));
    }

    /** RF-044: reenvía el código de verificación (máximo uno por minuto y cinco cada 30 minutos). */
    @Operation(summary = "Reenviar el código OTP (RF-044)",
            description = "Público. Solo envía un código si la persona validó su contraseña hace menos de 30 minutos. "
                    + "Responde 429 si se pide antes de 60 s o tras 5 reenvíos.")
    @PostMapping("/otp/reenviar")
    public LoginRespuesta reenviarOtp(@Valid @RequestBody ReenviarOtpRequest req, HttpServletRequest http) {
        return auth.reenviarOtp(req, IpUtil.de(http));
    }

    @PostMapping("/recuperar")
    public RecuperarRespuesta recuperar(@Valid @RequestBody RecuperarRequest req, HttpServletRequest http) {
        return recuperacion.solicitar(req, IpUtil.de(http));
    }

    @PostMapping("/restablecer")
    public MensajeRespuesta restablecer(@Valid @RequestBody RestablecerRequest req, HttpServletRequest http) {
        return recuperacion.restablecer(req, IpUtil.de(http));
    }

    /** RF-043: canjea el enlace de desbloqueo de un solo uso. */
    @Operation(summary = "Desbloquear la cuenta con el enlace del correo (RF-043)",
            description = "Público. El enlace es de un solo uso y vence a los 15 minutos. Responde 400 si venció o ya "
                    + "se usó y 403 si la cuenta solo la puede desbloquear un administrador.")
    @PostMapping("/desbloquear")
    public MensajeRespuesta desbloquear(@Valid @RequestBody DesbloquearRequest req, HttpServletRequest http) {
        return desbloqueo.desbloquear(req.token(), IpUtil.de(http));
    }

    /** RF-043: pide un enlace nuevo si el anterior venció o ya se usó. */
    @Operation(summary = "Pedir un nuevo enlace de desbloqueo (RF-043)",
            description = "Público. Responde siempre el mismo mensaje para no revelar qué cuentas existen; 429 si se "
                    + "pide otro antes de 60 s. En modo desarrollo devuelve el enlace.")
    @PostMapping("/desbloqueo/solicitar")
    public DesbloqueoRespuesta solicitarDesbloqueo(@Valid @RequestBody DesbloqueoSolicitudRequest req,
                                                   HttpServletRequest http) {
        return desbloqueo.solicitar(req.correo(), IpUtil.de(http));
    }

    @GetMapping("/me")
    public PerfilActual me(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return auth.perfilActual(principal);
    }

    @PostMapping("/primer-acceso")
    public PerfilActual primerAcceso(@AuthenticationPrincipal UsuarioPrincipal principal,
                                     @Valid @RequestBody PrimerAccesoRequest req, HttpServletRequest http) {
        return auth.primerAcceso(principal, req, IpUtil.de(http));
    }

    /** El estudiante activa o desactiva su segundo factor; para instructor y administrador responde 403. */
    @PutMapping("/doble-factor")
    public PerfilActual dobleFactor(@AuthenticationPrincipal UsuarioPrincipal principal,
                                    @Valid @RequestBody DobleFactorRequest req, HttpServletRequest http) {
        return auth.cambiarDobleFactor(principal, req, IpUtil.de(http));
    }

    /** RF-042: amplía la sesión (renueva el token) cuando la persona responde al aviso de tiempo. */
    @Operation(summary = "Ampliar la sesión (RF-042)",
            description = "Renueva la sesión deslizante y emite un JWT nuevo para la misma sesión; el anterior deja de "
                    + "valer. Cada respuesta autenticada trae la cabecera X-Sesion-Restante con los segundos que quedan.")
    @PostMapping("/refresh")
    public RenovacionRespuesta refresh(@AuthenticationPrincipal UsuarioPrincipal principal, HttpServletRequest http) {
        return auth.renovarSesion(principal, IpUtil.de(http));
    }

    @PostMapping("/logout")
    public MensajeRespuesta logout(@AuthenticationPrincipal UsuarioPrincipal principal, HttpServletRequest http) {
        auth.cerrarSesion(principal, IpUtil.de(http));
        return new MensajeRespuesta("Cerraste sesión.");
    }
}
