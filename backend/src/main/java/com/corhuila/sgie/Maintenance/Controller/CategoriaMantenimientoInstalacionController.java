package com.corhuila.sgie.Maintenance.Controller;

import com.corhuila.sgie.Maintenance.Entity.CategoriaMantenimientoInstalacion;
import com.corhuila.sgie.Maintenance.Service.CategoriaMantenimientoInstalacionService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/categoria-mantenimiento-instalacion")
public class CategoriaMantenimientoInstalacionController extends BaseController<CategoriaMantenimientoInstalacion, CategoriaMantenimientoInstalacionService> {
    public CategoriaMantenimientoInstalacionController(CategoriaMantenimientoInstalacionService service) {
        super(service, "CATEGORIA_MANTENIMIENTO_INSTALACION");
    }
}
