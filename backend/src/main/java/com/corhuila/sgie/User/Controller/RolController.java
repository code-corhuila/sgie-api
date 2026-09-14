package com.corhuila.sgie.User.Controller;

import com.corhuila.sgie.User.Entity.Rol;
import com.corhuila.sgie.User.IService.IRolService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/rol")
public class RolController extends BaseController<Rol, IRolService> {
    public RolController(IRolService service) {
        super(service, "ROL");
    }
}
