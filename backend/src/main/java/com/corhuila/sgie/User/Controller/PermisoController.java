package com.corhuila.sgie.User.Controller;

import com.corhuila.sgie.User.Entity.Permiso;
import com.corhuila.sgie.User.IService.IPermisoService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/permiso")
public class PermisoController extends BaseController<Permiso, IPermisoService> {
    public PermisoController(IPermisoService service) {
        super(service, "PERMISO");
    }
}
