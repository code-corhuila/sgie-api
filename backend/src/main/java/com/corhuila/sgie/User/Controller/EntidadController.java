package com.corhuila.sgie.User.Controller;

import com.corhuila.sgie.User.Entity.Entidad;
import com.corhuila.sgie.User.IService.IEntidadService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/entidad")
public class EntidadController extends BaseController<Entidad, IEntidadService> {
    public EntidadController(IEntidadService service) {
        super(service, "ENTIDAD");
    }
}
