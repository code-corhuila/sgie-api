package com.corhuila.sgie.User.Controller;

import com.corhuila.sgie.User.DTO.IPersonaUsuarioDTO;
import com.corhuila.sgie.User.Entity.Persona;
import com.corhuila.sgie.User.IService.IPersonaService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/api/persona")
public class PersonaController extends BaseController<Persona, IPersonaService> {
    public PersonaController(IPersonaService service) {
        super(service, "PERSONA");
    }

    @GetMapping("/persona-usuario")
    @PreAuthorize("@permissionEvaluator.hasPermission(authentication, this.entityName, 'CONSULTAR')")
    public ResponseEntity<List<IPersonaUsuarioDTO>> findUsuariosPersonaPorIdentificacion(@RequestParam String numeroIdentificacion) {
        List<IPersonaUsuarioDTO> personaUsuarios = service.findUsuariosPersonaPorIdentificacion(numeroIdentificacion);
        return ResponseEntity.ok(personaUsuarios);
    }
}
