--
-- PostgreSQL database dump


-- Dumped from database version 17.6 (Postgres.app)
-- Dumped by pg_dump version 17.6 (Postgres.app)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: horas_disponibles_equipo(date, integer, bigint, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.horas_disponibles_equipo(p_fecha date, p_idequipo integer, p_iddetalle bigint DEFAULT NULL::bigint, p_origen text DEFAULT NULL::text) RETURNS TABLE(hora time without time zone)
    LANGUAGE plpgsql STABLE
    AS $$
BEGIN
    RETURN QUERY
    WITH horas AS (
        SELECT generate_series(
            (p_fecha::timestamp + time '05:00'),
            (p_fecha::timestamp + time '21:00'),   -- si quieres último inicio válido, cámbialo a '20:00'
            interval '1 hour'
        ) AS hora
    ),
    ocupadas AS (
        -- Reservas activas (equipo)
        SELECT r.hora_inicio,
               r.hora_fin,
               de.id       AS id_detalle,
               'RESERVA'   AS origen
        FROM reserva r
        INNER JOIN detalle_reserva_equipo de ON r.id = de.id_reserva
        WHERE r.fecha_reserva = p_fecha
          AND de.id_equipo = p_idEquipo
          AND COALESCE(r.state, false) = true
          AND COALESCE(de.state, false) = true

        UNION ALL

        -- Mantenimientos activos (equipo)
        SELECT r.hora_inicio,
               r.hora_fin,
               me.id       AS id_detalle,
               'MANTENIMIENTO' AS origen
        FROM reserva r
        INNER JOIN mantenimiento_equipo me ON r.id = me.id_reserva
        WHERE r.fecha_reserva = p_fecha
          AND me.id_equipo = p_idEquipo
          AND COALESCE(r.state, false) = true
          AND COALESCE(me.state, false) = true
    )
    SELECT h.hora::time
    FROM horas h
    WHERE NOT EXISTS (
        SELECT 1
        FROM ocupadas o
        WHERE h.hora::time >= o.hora_inicio
          AND h.hora::time <  o.hora_fin
          AND NOT (
              p_idDetalle IS NOT NULL
              AND o.id_detalle = p_idDetalle
              AND (p_origen IS NULL OR o.origen = p_origen)
          )
    )
    ORDER BY h.hora;
END;
$$;


ALTER FUNCTION public.horas_disponibles_equipo(p_fecha date, p_idequipo integer, p_iddetalle bigint, p_origen text) OWNER TO postgres;

--
-- Name: horas_disponibles_instalacion(date, integer, bigint, text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.horas_disponibles_instalacion(p_fecha date, p_idinstalacion integer, p_iddetalle bigint DEFAULT NULL::bigint, p_origen text DEFAULT NULL::text) RETURNS TABLE(hora time without time zone)
    LANGUAGE plpgsql STABLE
    AS $$
BEGIN
    RETURN QUERY
    WITH horas AS (
        SELECT generate_series(
            (p_fecha::timestamp + time '05:00'),
            (p_fecha::timestamp + time '21:00'),
            interval '1 hour'
        ) AS hora
    ),
    ocupadas AS (
        SELECT r.hora_inicio,
               r.hora_fin,
               di.id      AS id_detalle,
               'RESERVA'  AS origen
        FROM reserva r
        INNER JOIN detalle_reserva_instalacion di ON r.id = di.id_reserva
        WHERE r.fecha_reserva = p_fecha
          AND di.id_instalacion = p_idInstalacion
          AND COALESCE(r.state, false) = true
          AND COALESCE(di.state, false) = true

        UNION ALL

        SELECT r.hora_inicio,
               r.hora_fin,
               mi.id      AS id_detalle,
               'MANTENIMIENTO' AS origen
        FROM reserva r
        INNER JOIN mantenimiento_instalacion mi ON r.id = mi.id_reserva
        WHERE r.fecha_reserva = p_fecha
          AND mi.id_instalacion = p_idInstalacion
          AND COALESCE(r.state, false) = true
          AND COALESCE(mi.state, false) = true
    )
    SELECT h.hora::time
    FROM horas h
    WHERE NOT EXISTS (
        SELECT 1
        FROM ocupadas o
        WHERE h.hora::time >= o.hora_inicio
          AND h.hora::time < o.hora_fin
          AND NOT (
              p_idDetalle IS NOT NULL
              AND o.id_detalle = p_idDetalle
			  AND (p_origen IS NULL OR o.origen = p_origen)
			  --AND p_origen IS NOT NULL
              --AND o.origen = p_origen
			  
          )
    )
    ORDER BY h.hora;
END;
$$;


ALTER FUNCTION public.horas_disponibles_instalacion(p_fecha date, p_idinstalacion integer, p_iddetalle bigint, p_origen text) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: campus; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.campus (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255),
    id_municipio bigint
);


ALTER TABLE public.campus OWNER TO sebastianzambrano;

--
-- Name: campus_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.campus ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.campus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: categoria_equipo; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.categoria_equipo (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255)
);


ALTER TABLE public.categoria_equipo OWNER TO sebastianzambrano;

--
-- Name: categoria_equipo_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.categoria_equipo ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.categoria_equipo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: categoria_instalacion; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.categoria_instalacion (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255)
);


ALTER TABLE public.categoria_instalacion OWNER TO sebastianzambrano;

--
-- Name: categoria_instalacion_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.categoria_instalacion ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.categoria_instalacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: categoria_mantenimiento_equipo; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.categoria_mantenimiento_equipo (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255)
);


ALTER TABLE public.categoria_mantenimiento_equipo OWNER TO sebastianzambrano;

--
-- Name: categoria_mantenimiento_equipo_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.categoria_mantenimiento_equipo ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.categoria_mantenimiento_equipo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: categoria_mantenimiento_instalacion; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.categoria_mantenimiento_instalacion (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255)
);


ALTER TABLE public.categoria_mantenimiento_instalacion OWNER TO sebastianzambrano;

--
-- Name: categoria_mantenimiento_instalacion_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.categoria_mantenimiento_instalacion ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.categoria_mantenimiento_instalacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: continente; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.continente (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255)
);


ALTER TABLE public.continente OWNER TO sebastianzambrano;

--
-- Name: continente_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.continente ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.continente_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: departamento; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.departamento (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255),
    id_pais bigint
);


ALTER TABLE public.departamento OWNER TO sebastianzambrano;

--
-- Name: departamento_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.departamento ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.departamento_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: detalle_reserva_equipo; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.detalle_reserva_equipo (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    entrega_equipo character varying(255),
    numero_estudiantes smallint,
    programa_academico character varying(255),
    id_equipo bigint NOT NULL,
    id_instalacion_destino bigint,
    id_reserva bigint NOT NULL
);


ALTER TABLE public.detalle_reserva_equipo OWNER TO sebastianzambrano;

--
-- Name: detalle_reserva_equipo_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.detalle_reserva_equipo ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.detalle_reserva_equipo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: detalle_reserva_instalacion; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.detalle_reserva_instalacion (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    entrega_instalacion character varying(255),
    numero_estudiantes smallint,
    programa_academico character varying(255),
    id_instalacion bigint,
    id_reserva bigint
);


ALTER TABLE public.detalle_reserva_instalacion OWNER TO sebastianzambrano;

--
-- Name: detalle_reserva_instalacion_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.detalle_reserva_instalacion ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.detalle_reserva_instalacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: entidad; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.entidad (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    nombre character varying(255)
);


ALTER TABLE public.entidad OWNER TO sebastianzambrano;

--
-- Name: entidad_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.entidad ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.entidad_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: equipo; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.equipo (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    codigo character varying(255) NOT NULL,
    id_instalacion bigint NOT NULL,
    id_tipo_equipo bigint NOT NULL
);


ALTER TABLE public.equipo OWNER TO sebastianzambrano;

--
-- Name: equipo_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.equipo ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.equipo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: instalacion; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.instalacion (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255),
    id_campus bigint,
    id_categoria_instalacion bigint
);


ALTER TABLE public.instalacion OWNER TO sebastianzambrano;

--
-- Name: instalacion_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.instalacion ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.instalacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: mantenimiento_equipo; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.mantenimiento_equipo (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    fecha_proxima_mantenimiento date,
    resultado_mantenimiento character varying(255),
    id_categoria_mantenimiento_equipo bigint NOT NULL,
    id_equipo bigint NOT NULL,
    id_reserva bigint NOT NULL
);


ALTER TABLE public.mantenimiento_equipo OWNER TO sebastianzambrano;

--
-- Name: mantenimiento_equipo_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.mantenimiento_equipo ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.mantenimiento_equipo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: mantenimiento_instalacion; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.mantenimiento_instalacion (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    fecha_proxima_mantenimiento date,
    resultado_mantenimiento character varying(255),
    id_categoria_mantenimiento_instalacion bigint NOT NULL,
    id_instalacion bigint NOT NULL,
    id_reserva bigint NOT NULL
);


ALTER TABLE public.mantenimiento_instalacion OWNER TO sebastianzambrano;

--
-- Name: mantenimiento_instalacion_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.mantenimiento_instalacion ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.mantenimiento_instalacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: municipio; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.municipio (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255),
    id_departamento bigint
);


ALTER TABLE public.municipio OWNER TO sebastianzambrano;

--
-- Name: municipio_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.municipio ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.municipio_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: pais; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.pais (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255),
    id_continente bigint
);


ALTER TABLE public.pais OWNER TO sebastianzambrano;

--
-- Name: pais_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.pais ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.pais_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: permiso; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.permiso (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255)
);


ALTER TABLE public.permiso OWNER TO sebastianzambrano;

--
-- Name: permiso_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.permiso ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.permiso_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: permiso_rol; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.permiso_rol (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    id_permiso bigint,
    id_rol bigint
);


ALTER TABLE public.permiso_rol OWNER TO sebastianzambrano;

--
-- Name: permiso_rol_entidad; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.permiso_rol_entidad (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    id_entidad bigint,
    id_permiso bigint,
    id_rol bigint
);


ALTER TABLE public.permiso_rol_entidad OWNER TO sebastianzambrano;

--
-- Name: permiso_rol_entidad_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.permiso_rol_entidad ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.permiso_rol_entidad_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: permiso_rol_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.permiso_rol ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.permiso_rol_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: persona; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.persona (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    apellidos character varying(255),
    nombres character varying(255),
    numero_identificacion character varying(255) NOT NULL,
    telefono_movil character varying(255),
    tipo_documento character varying(255),
    id_rol bigint
);


ALTER TABLE public.persona OWNER TO sebastianzambrano;

--
-- Name: persona_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.persona ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.persona_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: reserva; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.reserva (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    fecha_reserva date,
    hora_fin time(6) without time zone,
    hora_inicio time(6) without time zone,
    nombre character varying(255),
    id_persona bigint,
    id_tipo_reserva bigint NOT NULL
);


ALTER TABLE public.reserva OWNER TO sebastianzambrano;

--
-- Name: reserva_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.reserva ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.reserva_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: rol; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.rol (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255)
);


ALTER TABLE public.rol OWNER TO sebastianzambrano;

--
-- Name: rol_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.rol ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.rol_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_equipo; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.tipo_equipo (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion character varying(255),
    nombre character varying(255),
    id_categoria_equipo bigint NOT NULL
);


ALTER TABLE public.tipo_equipo OWNER TO sebastianzambrano;

--
-- Name: tipo_equipo_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.tipo_equipo ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tipo_equipo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_reserva; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.tipo_reserva (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    descripcion text,
    nombre character varying(100) NOT NULL,
    requiere_aprobacion boolean
);


ALTER TABLE public.tipo_reserva OWNER TO sebastianzambrano;

--
-- Name: tipo_reserva_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.tipo_reserva ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.tipo_reserva_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: sebastianzambrano
--

CREATE TABLE public.usuario (
    id bigint NOT NULL,
    fecha_creacion timestamp(6) without time zone NOT NULL,
    usuario_creacion bigint,
    fecha_eliminacion timestamp(6) without time zone,
    usuario_eliminacion bigint,
    state boolean,
    fecha_modificacion timestamp(6) without time zone NOT NULL,
    usuario_modificacion bigint,
    email character varying(255) NOT NULL,
    password character varying(255),
    id_persona bigint NOT NULL
);


ALTER TABLE public.usuario OWNER TO sebastianzambrano;

--
-- Name: usuario_id_seq; Type: SEQUENCE; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE public.usuario ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.usuario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: campus; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.campus (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre, id_municipio) FROM stdin;
1	2025-11-03 14:54:33.541648	\N	\N	\N	t	2025-11-03 15:02:46.893525	1	Sede de administracion de empresas, negocios internacionales y MVZ	CAMPUS QUIRINAL	9
2	2025-11-03 14:54:33.541648	\N	\N	\N	t	2025-11-03 15:03:25.117275	1	Sede de ingenieria	CAMPUS PRADO ALTO	9
3	2025-11-03 15:03:59.565029	1	\N	\N	t	2025-11-03 15:03:59.565036	\N	campus sur	CAMPUS SUR	9
\.


--
-- Data for Name: categoria_equipo; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.categoria_equipo (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre) FROM stdin;
1	2025-11-03 14:54:57.997631	1	\N	\N	t	2025-11-03 14:54:57.997631	\N	Categoría para equipos computo	Equipos de computo
2	2025-11-03 14:55:01.225642	1	\N	\N	t	2025-11-03 14:55:01.225642	\N	Categoría para equipos usados en prácticas y experimentos de telematica	Equipos de telematica
3	2025-11-03 14:55:05.448874	1	\N	\N	t	2025-11-03 14:55:05.448874	\N	Categoría para equipos usados para actividades audivisuales	Equipos audivisuales
4	2025-11-03 14:55:05.448874	1	\N	\N	t	2025-11-03 14:55:05.448874	\N	Categoría para equipos usados en prácticas y experimentos	Equipos de Laboratorio
5	2025-11-03 15:50:37.519614	1	\N	\N	t	2025-11-03 15:50:37.519617	\N	herramientas de construcción	HERRAMIENTAS DE CONSTRUCCION
\.


--
-- Data for Name: categoria_instalacion; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.categoria_instalacion (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre) FROM stdin;
1	2025-11-03 14:53:47.965947	1	\N	\N	t	2025-11-03 14:53:47.965947	\N	Instalaciones destinadas a prácticas de laboratorio	Laboratorio
2	2025-11-03 14:53:47.965947	1	\N	\N	t	2025-11-03 14:53:47.965947	\N	Instalaciones destinadas para dar clases	aulas
3	2025-11-03 14:53:47.965947	1	\N	\N	t	2025-11-03 14:53:47.965947	\N	Instalaciones destinadas a eventos masivos	auditorio
4	2025-11-03 14:53:47.965947	1	\N	\N	t	2025-11-03 14:53:47.965947	\N	Instalaciones destinadas para almacenamiento de equipos y materiales academicos	bodega
5	2025-11-03 15:05:01.090012	1	\N	\N	t	2025-11-03 15:05:01.090028	\N	cateferia	CAFETERIA
\.


--
-- Data for Name: categoria_mantenimiento_equipo; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.categoria_mantenimiento_equipo (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre) FROM stdin;
1	2025-11-03 14:53:14.053308	1	\N	\N	t	2025-11-03 14:53:14.053308	1	Mantenimiento programado para evitar fallas o averías.	PREVENTIVO
2	2025-11-03 14:53:14.053308	1	\N	\N	t	2025-11-03 14:53:14.053308	1	Mantenimiento realizado para reparar fallas detectadas.	CORRECTIVO
3	2025-11-03 14:53:14.053308	1	\N	\N	t	2025-11-03 14:53:14.053308	1	Mantenimiento basado en el análisis de condiciones para predecir fallas.	PREDICTIVO
\.


--
-- Data for Name: categoria_mantenimiento_instalacion; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.categoria_mantenimiento_instalacion (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre) FROM stdin;
1	2025-11-03 14:53:17.790642	1	\N	\N	t	2025-11-03 14:53:17.790642	1	Mantenimiento programado para evitar fallas o averías.	Preventivo
2	2025-11-03 14:53:17.790642	1	\N	\N	t	2025-11-03 14:53:17.790642	1	Mantenimiento realizado para reparar fallas detectadas.	Correctivo
3	2025-11-03 14:53:17.790642	1	\N	\N	t	2025-11-03 14:53:17.790642	1	Mantenimiento basado en el análisis de condiciones para predecir fallas.	Predictivo
\.


--
-- Data for Name: continente; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.continente (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre) FROM stdin;
1	2025-11-03 14:53:27.705074	1	\N	\N	t	2025-11-03 14:53:27.705074	\N	Continente africano	África
2	2025-11-03 14:53:27.705074	1	\N	\N	t	2025-11-03 14:53:27.705074	\N	Continente norteamericano	América del Norte
3	2025-11-03 14:53:27.705074	1	\N	\N	t	2025-11-03 14:53:27.705074	\N	Continente centroamericano	América Central
4	2025-11-03 14:53:27.705074	1	\N	\N	t	2025-11-03 14:53:27.705074	\N	Continente sudamericano	América del Sur
5	2025-11-03 14:53:27.705074	1	\N	\N	t	2025-11-03 14:53:27.705074	\N	Continente antártico	Antártida
6	2025-11-03 14:53:27.705074	1	\N	\N	t	2025-11-03 14:53:27.705074	\N	Continente asiático	Asia
7	2025-11-03 14:53:27.705074	1	\N	\N	t	2025-11-03 14:53:27.705074	\N	Continente europeo	Europa
8	2025-11-03 14:53:27.705074	1	\N	\N	t	2025-11-03 14:53:27.705074	\N	Continente oceánico	Oceanía
\.


--
-- Data for Name: departamento; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.departamento (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre, id_pais) FROM stdin;
1	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Amazonas	Amazonas	1
2	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Antioquia	Antioquia	1
3	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Atlántico	Atlántico	1
4	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Bolívar	Bolívar	1
5	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Boyacá	Boyacá	1
6	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Caldas	Caldas	1
7	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Cauca	Cauca	1
8	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Cundinamarca	Cundinamarca	1
9	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Huila	Huila	1
10	2025-11-03 14:53:36.63109	1	\N	\N	t	2025-11-03 14:53:36.63109	\N	Departamento de Valle del Cauca	Valle del Cauca	1
\.


--
-- Data for Name: detalle_reserva_equipo; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.detalle_reserva_equipo (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, entrega_equipo, numero_estudiantes, programa_academico, id_equipo, id_instalacion_destino, id_reserva) FROM stdin;
1	2025-11-03 16:42:03.746969	1	\N	\N	f	2025-11-03 19:34:23.699989	1	PRUEBA	30	PRUEBA 3	1	1	4
\.


--
-- Data for Name: detalle_reserva_instalacion; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.detalle_reserva_instalacion (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, entrega_instalacion, numero_estudiantes, programa_academico, id_instalacion, id_reserva) FROM stdin;
2	2025-11-03 19:22:40.430731	1	\N	\N	t	2025-11-03 19:22:40.430733	\N	\N	30	PRUEBA 6	1	7
1	2025-11-03 16:43:49.157584	1	\N	\N	f	2025-11-03 19:34:15.853689	1	PRUEBA	30	PRUEBA 4	1	5
\.


--
-- Data for Name: entidad; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.entidad (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, nombre) FROM stdin;
1	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	ENTIDAD
2	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	PERMISO
3	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	ROL
4	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	PERSONA
5	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	USUARIO
6	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	PERMISO_ROL_ENTIDAD
7	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	CONTINENTE
8	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	PAIS
9	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	DEPARTAMENTO
10	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	MUNICIPIO
11	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	CAMPUS
12	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	CATEGORIA_INSTALACION
13	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	INSTALACION
14	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	CATEGORIA_MANTENIMIENTO_EQUIPO
15	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	CATEGORIA_MANTENIMIENTO_INSTALACION
16	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	MANTENIMIENTO_EQUIPO
17	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	MANTENIMIENTO_INSTALACION
18	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	CATEGORIA_EQUIPO
19	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	EQUIPO
20	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	TIPO_EQUIPO
21	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	TIPO_RESERVA
22	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	RESERVA
23	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	DETALLE_RESERVA_EQUIPO
24	2025-11-03 14:53:00.127422	1	\N	\N	t	2025-11-03 14:53:00.127422	\N	DETALLE_RESERVA_INSTALACION
\.


--
-- Data for Name: equipo; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.equipo (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, codigo, id_instalacion, id_tipo_equipo) FROM stdin;
1	2025-11-03 14:55:15.153728	1	\N	\N	t	2025-11-03 14:55:15.153728	\N	EQ-0001	1	1
2	2025-11-03 14:55:15.153728	1	\N	\N	t	2025-11-03 14:55:15.153728	\N	EQ-0002	1	1
3	2025-11-03 14:55:15.153728	1	\N	\N	t	2025-11-03 14:55:15.153728	\N	EQ-0003	1	1
4	2025-11-03 15:51:23.194818	\N	\N	\N	t	2025-11-03 15:52:52.357082	1	M1	7	4
\.


--
-- Data for Name: instalacion; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.instalacion (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre, id_campus, id_categoria_instalacion) FROM stdin;
1	2025-11-03 14:54:39.050809	1	\N	\N	t	2025-11-03 14:54:39.050809	\N	Instalación equipada para prácticas tecnologicas	Laboratorio de informatica 	2	1
2	2025-11-03 14:54:44.323105	1	\N	\N	t	2025-11-03 14:54:44.323105	\N	salon bloque c	salon c-201	2	2
3	2025-11-03 14:54:47.82109	1	\N	\N	t	2025-11-03 14:54:47.82109	\N	salon bloque c	salon c-202	2	2
4	2025-11-03 14:54:51.598869	1	\N	\N	t	2025-11-03 14:54:51.598869	\N	salon bloque c	salon c-203	2	2
5	2025-11-03 14:54:54.864489	1	\N	\N	t	2025-11-03 14:54:54.864489	\N	instalacion destinada para almacenar equipos tecnologicos	bodega	2	2
6	2025-11-03 15:04:31.29716	\N	\N	\N	t	2025-11-03 15:14:09.381336	1	laboratorio destinado al sur	LABORATORIO SUR	3	1
7	2025-11-03 15:48:30.474443	1	\N	\N	t	2025-11-03 15:48:30.475216	\N	cafeteria	CAFETERIA SUR A	3	5
\.


--
-- Data for Name: mantenimiento_equipo; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.mantenimiento_equipo (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, fecha_proxima_mantenimiento, resultado_mantenimiento, id_categoria_mantenimiento_equipo, id_equipo, id_reserva) FROM stdin;
2	2025-11-03 16:35:17.387227	1	\N	\N	f	2025-11-03 19:34:07.340987	1	PRUEBA 2	2025-11-03	PRUEBA	1	1	3
3	2025-11-03 19:27:12.634434	1	\N	\N	t	2025-11-06 21:14:15.42319	1	PRUEBA 7	\N	\N	1	1	8
\.


--
-- Data for Name: mantenimiento_instalacion; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.mantenimiento_instalacion (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, fecha_proxima_mantenimiento, resultado_mantenimiento, id_categoria_mantenimiento_instalacion, id_instalacion, id_reserva) FROM stdin;
1	2025-11-03 15:57:19.232926	1	\N	\N	t	2025-11-03 19:33:44.573986	1	PRUEBA 1	\N	\N	1	1	1
2	2025-11-03 17:19:05.293692	1	\N	\N	f	2025-11-03 19:33:56.425134	1	PRUEBA 5	2025-11-03	PRUEBA	1	1	6
\.


--
-- Data for Name: municipio; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.municipio (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre, id_departamento) FROM stdin;
1	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital del Amazonas	Leticia	1
2	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital de Antioquia	Medellín	2
3	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital del Atlántico	Barranquilla	3
4	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital de Bolívar	Cartagena de Indias	4
5	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital de Boyacá	Tunja	5
6	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital de Caldas	Manizales	6
7	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital de Cauca	Popayán	7
8	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital de Cundinamarca y del país	Bogotá	8
9	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital del Huila	Neiva	9
10	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Segundo municipio más poblado del Huila	Pitalito	9
11	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Municipio del centro del Huila	Garzón	9
12	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Municipio del occidente del Huila	La Plata	9
13	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Municipio agrícola del Huila	Campoalegre	9
14	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Municipio arqueológico del Huila	San Agustín	9
15	2025-11-03 14:53:42.522836	1	\N	\N	t	2025-11-03 14:53:42.522836	\N	Capital de Valle del Cauca	Cali	10
\.


--
-- Data for Name: pais; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.pais (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre, id_continente) FROM stdin;
1	2025-11-03 14:53:33.109276	1	\N	\N	t	2025-11-03 14:53:33.109276	\N	País en América del Sur	Colombia	4
\.


--
-- Data for Name: permiso; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.permiso (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre) FROM stdin;
1	2025-11-03 14:52:41.201364	1	\N	\N	t	2025-11-03 14:52:41.201364	\N	Permite crear registros	CREAR
2	2025-11-03 14:52:41.201364	1	\N	\N	t	2025-11-03 14:52:41.201364	\N	Permite consultar registros	CONSULTAR
3	2025-11-03 14:52:41.201364	1	\N	\N	t	2025-11-03 14:52:41.201364	\N	Permite actualizar registros	ACTUALIZAR
4	2025-11-03 14:52:41.201364	1	\N	\N	t	2025-11-03 14:52:41.201364	\N	Permite eliminar registros	ELIMINAR
\.


--
-- Data for Name: permiso_rol; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.permiso_rol (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, id_permiso, id_rol) FROM stdin;
\.


--
-- Data for Name: permiso_rol_entidad; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.permiso_rol_entidad (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, id_entidad, id_permiso, id_rol) FROM stdin;
1	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	1	1	1
2	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	1	2	1
3	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	1	3	1
4	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	1	4	1
5	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	2	1	1
6	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	2	2	1
7	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	2	3	1
8	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	2	4	1
9	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	3	1	1
10	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	3	2	1
11	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	3	3	1
12	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	3	4	1
13	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	4	1	1
14	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	4	2	1
15	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	4	3	1
16	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	4	4	1
17	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	5	1	1
18	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	5	2	1
19	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	5	3	1
20	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	5	4	1
21	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	6	1	1
22	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	6	2	1
23	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	6	3	1
24	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	6	4	1
25	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	7	1	1
26	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	7	2	1
27	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	7	3	1
28	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	7	4	1
29	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	8	1	1
30	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	8	2	1
31	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	8	3	1
32	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	8	4	1
33	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	9	1	1
34	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	9	2	1
35	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	9	3	1
36	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	9	4	1
37	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	10	1	1
38	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	10	2	1
39	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	10	3	1
40	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	10	4	1
41	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	11	1	1
42	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	11	2	1
43	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	11	3	1
44	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	11	4	1
45	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	12	1	1
46	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	12	2	1
47	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	12	3	1
48	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	12	4	1
49	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	13	1	1
50	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	13	2	1
51	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	13	3	1
52	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	13	4	1
53	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	14	1	1
54	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	14	2	1
55	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	14	3	1
56	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	14	4	1
57	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	15	1	1
58	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	15	2	1
59	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	15	3	1
60	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	15	4	1
61	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	16	1	1
62	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	16	2	1
63	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	16	3	1
64	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	16	4	1
65	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	17	1	1
66	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	17	2	1
67	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	17	3	1
68	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	17	4	1
69	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	18	1	1
70	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	18	2	1
71	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	18	3	1
72	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	18	4	1
73	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	19	1	1
74	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	19	2	1
75	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	19	3	1
76	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	19	4	1
77	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	20	1	1
78	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	20	2	1
79	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	20	3	1
80	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	20	4	1
81	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	21	1	1
82	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	21	2	1
83	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	21	3	1
84	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	21	4	1
85	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	22	1	1
86	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	22	2	1
87	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	22	3	1
88	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	22	4	1
89	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	23	1	1
90	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	23	2	1
91	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	23	3	1
92	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	23	4	1
93	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	24	1	1
94	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	24	2	1
95	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	24	3	1
96	2025-11-03 14:53:21.613853	1	\N	\N	t	2025-11-03 14:53:21.613853	\N	24	4	1
\.


--
-- Data for Name: persona; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.persona (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, apellidos, nombres, numero_identificacion, telefono_movil, tipo_documento, id_rol) FROM stdin;
1	2025-11-03 14:53:04.932867	1	\N	\N	t	2025-11-03 14:53:04.932867	\N	PRINCIPAL	ADMIN	1234567890	3001234567	CÉDULA DE CIUDADANÍA	1
2	2025-11-03 14:57:03.666536	\N	\N	\N	t	2025-11-03 14:57:20.551162	1	ZAMBRANO	CAMILO	1075297947	3227230065	CÉDULA DE CIUDADANÍA	3
\.


--
-- Data for Name: reserva; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.reserva (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, fecha_reserva, hora_fin, hora_inicio, nombre, id_persona, id_tipo_reserva) FROM stdin;
7	2025-11-03 19:22:40.323811	1	\N	\N	t	2025-11-03 19:22:40.323815	\N	PRUEBA 6	2025-11-03	09:00:00	05:00:00	PRUEBA 6	2	4
1	2025-11-03 15:57:19.189501	\N	\N	\N	t	2025-11-03 19:33:44.574109	1	PRUEBA 1	2025-11-03	10:00:00	09:00:00	PRUEBA 1	2	1
6	2025-11-03 17:19:05.223473	1	\N	\N	f	2025-11-03 19:33:56.425247	1	PRUEBA 5	2025-11-03	11:00:00	10:00:00	PRUEBA 5	2	1
3	2025-11-03 16:35:17.22172	\N	\N	\N	f	2025-11-03 19:34:07.3411	1	PRUEBA 2	2025-11-03	06:00:00	05:00:00	PRUEBA 2	2	2
5	2025-11-03 16:43:49.109723	\N	\N	\N	f	2025-11-03 19:34:15.853784	1	PRUEBA 4	2025-11-03	12:00:00	11:00:00	PRUEBA 4	2	4
4	2025-11-03 16:42:03.686597	\N	\N	\N	f	2025-11-03 19:34:23.700137	1	PRUEBA 3	2025-11-03	09:00:00	07:00:00	PRUEBA 3	2	3
8	2025-11-03 19:27:12.584578	\N	\N	\N	t	2025-11-06 21:14:15.423271	1	PRUEBA 7	2025-11-05	08:00:00	05:00:00	PRUEBA 7	2	2
\.


--
-- Data for Name: rol; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.rol (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre) FROM stdin;
1	2025-11-03 14:52:26.887292	1	\N	\N	t	2025-11-03 14:52:26.887292	\N	Rol con acceso total al sistema	ADMINISTRADOR
2	2025-11-03 14:52:30.677193	1	\N	\N	t	2025-11-03 14:52:30.677193	\N	Rol con acceso requerido para gestionar reservas de instalaciones, equipos y reportes	COORDINADOR_RESERVAS
3	2025-11-03 14:52:33.641635	1	\N	\N	t	2025-11-03 14:52:33.641635	\N	Rol con acceso requerido para gestionar reservas de instalaciones y equipos	DOCENTE
4	2025-11-03 14:52:37.353217	1	\N	\N	t	2025-11-03 14:52:37.353217	\N	Rol con acceso requerido para gestionar reservas de instalaciones y equipos	ESTUDIANTE
\.


--
-- Data for Name: tipo_equipo; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.tipo_equipo (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre, id_categoria_equipo) FROM stdin;
1	2025-11-03 14:55:11.007365	1	\N	\N	t	2025-11-03 14:55:11.007365	\N	Equipo óptico para observación de muestras	Microscopio	4
2	2025-11-03 14:55:11.007365	1	\N	\N	t	2025-11-03 14:55:11.007365	\N	Equipo de computo con procesador I7, memoria ram de 32, mouse y teclado inhalambrico y disco duro de 512 gb SSD	computador de escritorio Asus	1
3	2025-11-03 14:55:11.007365	1	\N	\N	t	2025-11-03 14:55:11.007365	\N	Equipo para la reproducción de contenido audiovisual	proyectos Epson	3
4	2025-11-03 15:50:53.076522	1	\N	\N	t	2025-11-03 15:50:53.076539	\N	martillo de golpe	MARTILLO	5
\.


--
-- Data for Name: tipo_reserva; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.tipo_reserva (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, descripcion, nombre, requiere_aprobacion) FROM stdin;
1	2025-11-03 15:55:02.231112	1	\N	\N	t	2025-11-03 15:55:02.231112	\N	Reserva para mantenimiento preventivo o correctivo de instalaciones	MANTENIMIENTO INSTALACION	\N
2	2025-11-03 15:55:02.231112	1	\N	\N	t	2025-11-03 15:55:02.231112	\N	Reserva para mantenimiento preventivo o correctivo de equipos	MANTENIMIENTO EQUIPO	\N
3	2025-11-03 15:55:02.231112	1	\N	\N	t	2025-11-03 15:55:02.231112	\N	Reserva para uso de equipos por parte de los usuarios	RESERVA EQUIPO	\N
4	2025-11-03 15:55:02.231112	1	\N	\N	t	2025-11-03 15:55:02.231112	\N	Reserva para uso de instalaciones por parte de los usuarios	RESERVA INSTALACION	\N
\.


--
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: sebastianzambrano
--

COPY public.usuario (id, fecha_creacion, usuario_creacion, fecha_eliminacion, usuario_eliminacion, state, fecha_modificacion, usuario_modificacion, email, password, id_persona) FROM stdin;
1	2025-11-03 14:53:08.735968	1	\N	\N	t	2025-11-03 14:53:08.735968	\N	admin@system.com	$2b$12$CKG0.blMMABlrdrbo4wo4OfYEJELvQqtzxMzKS8KFT3PbYEFBg7hS	1
2	2025-11-03 14:57:44.298349	1	\N	\N	t	2025-11-03 15:00:50.753776	1	camiloz@gmail.com	$2b$12$CKG0.blMMABlrdrbo4wo4OfYEJELvQqtzxMzKS8KFT3PbYEFBg7hS	2
\.


--
-- Name: campus_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.campus_id_seq', 3, true);


--
-- Name: categoria_equipo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.categoria_equipo_id_seq', 5, true);


--
-- Name: categoria_instalacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.categoria_instalacion_id_seq', 5, true);


--
-- Name: categoria_mantenimiento_equipo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.categoria_mantenimiento_equipo_id_seq', 3, true);


--
-- Name: categoria_mantenimiento_instalacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.categoria_mantenimiento_instalacion_id_seq', 3, true);


--
-- Name: continente_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.continente_id_seq', 8, true);


--
-- Name: departamento_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.departamento_id_seq', 10, true);


--
-- Name: detalle_reserva_equipo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.detalle_reserva_equipo_id_seq', 1, true);


--
-- Name: detalle_reserva_instalacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.detalle_reserva_instalacion_id_seq', 2, true);


--
-- Name: entidad_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.entidad_id_seq', 25, true);


--
-- Name: equipo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.equipo_id_seq', 4, true);


--
-- Name: instalacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.instalacion_id_seq', 7, true);


--
-- Name: mantenimiento_equipo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.mantenimiento_equipo_id_seq', 3, true);


--
-- Name: mantenimiento_instalacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.mantenimiento_instalacion_id_seq', 2, true);


--
-- Name: municipio_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.municipio_id_seq', 15, true);


--
-- Name: pais_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.pais_id_seq', 1, true);


--
-- Name: permiso_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.permiso_id_seq', 4, true);


--
-- Name: permiso_rol_entidad_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.permiso_rol_entidad_id_seq', 100, true);


--
-- Name: permiso_rol_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.permiso_rol_id_seq', 1, false);


--
-- Name: persona_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.persona_id_seq', 2, true);


--
-- Name: reserva_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.reserva_id_seq', 8, true);


--
-- Name: rol_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.rol_id_seq', 4, true);


--
-- Name: tipo_equipo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.tipo_equipo_id_seq', 4, true);


--
-- Name: tipo_reserva_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.tipo_reserva_id_seq', 1, false);


--
-- Name: usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sebastianzambrano
--

SELECT pg_catalog.setval('public.usuario_id_seq', 2, true);


--
-- Name: campus campus_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.campus
    ADD CONSTRAINT campus_pkey PRIMARY KEY (id);


--
-- Name: categoria_equipo categoria_equipo_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.categoria_equipo
    ADD CONSTRAINT categoria_equipo_pkey PRIMARY KEY (id);


--
-- Name: categoria_instalacion categoria_instalacion_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.categoria_instalacion
    ADD CONSTRAINT categoria_instalacion_pkey PRIMARY KEY (id);


--
-- Name: categoria_mantenimiento_equipo categoria_mantenimiento_equipo_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.categoria_mantenimiento_equipo
    ADD CONSTRAINT categoria_mantenimiento_equipo_pkey PRIMARY KEY (id);


--
-- Name: categoria_mantenimiento_instalacion categoria_mantenimiento_instalacion_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.categoria_mantenimiento_instalacion
    ADD CONSTRAINT categoria_mantenimiento_instalacion_pkey PRIMARY KEY (id);


--
-- Name: continente continente_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.continente
    ADD CONSTRAINT continente_pkey PRIMARY KEY (id);


--
-- Name: departamento departamento_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.departamento
    ADD CONSTRAINT departamento_pkey PRIMARY KEY (id);


--
-- Name: detalle_reserva_equipo detalle_reserva_equipo_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.detalle_reserva_equipo
    ADD CONSTRAINT detalle_reserva_equipo_pkey PRIMARY KEY (id);


--
-- Name: detalle_reserva_instalacion detalle_reserva_instalacion_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.detalle_reserva_instalacion
    ADD CONSTRAINT detalle_reserva_instalacion_pkey PRIMARY KEY (id);


--
-- Name: entidad entidad_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.entidad
    ADD CONSTRAINT entidad_pkey PRIMARY KEY (id);


--
-- Name: equipo equipo_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.equipo
    ADD CONSTRAINT equipo_pkey PRIMARY KEY (id);


--
-- Name: instalacion instalacion_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.instalacion
    ADD CONSTRAINT instalacion_pkey PRIMARY KEY (id);


--
-- Name: mantenimiento_equipo mantenimiento_equipo_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_equipo
    ADD CONSTRAINT mantenimiento_equipo_pkey PRIMARY KEY (id);


--
-- Name: mantenimiento_instalacion mantenimiento_instalacion_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_instalacion
    ADD CONSTRAINT mantenimiento_instalacion_pkey PRIMARY KEY (id);


--
-- Name: municipio municipio_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.municipio
    ADD CONSTRAINT municipio_pkey PRIMARY KEY (id);


--
-- Name: pais pais_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.pais
    ADD CONSTRAINT pais_pkey PRIMARY KEY (id);


--
-- Name: permiso permiso_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.permiso
    ADD CONSTRAINT permiso_pkey PRIMARY KEY (id);


--
-- Name: permiso_rol_entidad permiso_rol_entidad_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.permiso_rol_entidad
    ADD CONSTRAINT permiso_rol_entidad_pkey PRIMARY KEY (id);


--
-- Name: permiso_rol permiso_rol_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.permiso_rol
    ADD CONSTRAINT permiso_rol_pkey PRIMARY KEY (id);


--
-- Name: persona persona_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.persona
    ADD CONSTRAINT persona_pkey PRIMARY KEY (id);


--
-- Name: reserva reserva_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_pkey PRIMARY KEY (id);


--
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id);


--
-- Name: tipo_equipo tipo_equipo_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.tipo_equipo
    ADD CONSTRAINT tipo_equipo_pkey PRIMARY KEY (id);


--
-- Name: tipo_reserva tipo_reserva_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.tipo_reserva
    ADD CONSTRAINT tipo_reserva_pkey PRIMARY KEY (id);


--
-- Name: equipo uk2ftv9vf8tfr41ofgte1oikqlt; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.equipo
    ADD CONSTRAINT uk2ftv9vf8tfr41ofgte1oikqlt UNIQUE (codigo);


--
-- Name: usuario uk33gathdlc33wn52w45op1r397; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT uk33gathdlc33wn52w45op1r397 UNIQUE (id_persona);


--
-- Name: usuario uk5171l57faosmj8myawaucatdw; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT uk5171l57faosmj8myawaucatdw UNIQUE (email);


--
-- Name: mantenimiento_instalacion ukhpjo7ucjkm7p45d9ihvp543s8; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_instalacion
    ADD CONSTRAINT ukhpjo7ucjkm7p45d9ihvp543s8 UNIQUE (id_reserva);


--
-- Name: mantenimiento_equipo ukpf9d7v4is5qw6r29ursrrniyc; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_equipo
    ADD CONSTRAINT ukpf9d7v4is5qw6r29ursrrniyc UNIQUE (id_reserva);


--
-- Name: persona ukqlnj424lpric1p681y6ecfin4; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.persona
    ADD CONSTRAINT ukqlnj424lpric1p681y6ecfin4 UNIQUE (numero_identificacion);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


--
-- Name: mantenimiento_instalacion fk3h4imjavyl3iehg3t935cmddx; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_instalacion
    ADD CONSTRAINT fk3h4imjavyl3iehg3t935cmddx FOREIGN KEY (id_categoria_mantenimiento_instalacion) REFERENCES public.categoria_mantenimiento_instalacion(id);


--
-- Name: permiso_rol_entidad fk5e5qjt7q36heu1rfb9ipow249; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.permiso_rol_entidad
    ADD CONSTRAINT fk5e5qjt7q36heu1rfb9ipow249 FOREIGN KEY (id_entidad) REFERENCES public.entidad(id);


--
-- Name: reserva fk637b8bhkmv3qho89uigrrpd6c; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT fk637b8bhkmv3qho89uigrrpd6c FOREIGN KEY (id_tipo_reserva) REFERENCES public.tipo_reserva(id);


--
-- Name: equipo fk688sataa8c8r7bkgh58gj6sdv; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.equipo
    ADD CONSTRAINT fk688sataa8c8r7bkgh58gj6sdv FOREIGN KEY (id_tipo_equipo) REFERENCES public.tipo_equipo(id);


--
-- Name: detalle_reserva_equipo fk6eytc6kdw37vw8h0jttdbeqkf; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.detalle_reserva_equipo
    ADD CONSTRAINT fk6eytc6kdw37vw8h0jttdbeqkf FOREIGN KEY (id_reserva) REFERENCES public.reserva(id);


--
-- Name: departamento fk80hqpdpt4nu6c4gfrw9yvp9m3; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.departamento
    ADD CONSTRAINT fk80hqpdpt4nu6c4gfrw9yvp9m3 FOREIGN KEY (id_pais) REFERENCES public.pais(id);


--
-- Name: instalacion fk88fd340464wy5g87tkidl1hja; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.instalacion
    ADD CONSTRAINT fk88fd340464wy5g87tkidl1hja FOREIGN KEY (id_categoria_instalacion) REFERENCES public.categoria_instalacion(id);


--
-- Name: tipo_equipo fk8p1hrcj59o8xfeq9i2ri5y84h; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.tipo_equipo
    ADD CONSTRAINT fk8p1hrcj59o8xfeq9i2ri5y84h FOREIGN KEY (id_categoria_equipo) REFERENCES public.categoria_equipo(id);


--
-- Name: reserva fk9xrk2vko55b586arn4vjt9wlc; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT fk9xrk2vko55b586arn4vjt9wlc FOREIGN KEY (id_persona) REFERENCES public.persona(id);


--
-- Name: usuario fkagix3q8yqktlyj3yp1sn0mcd9; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT fkagix3q8yqktlyj3yp1sn0mcd9 FOREIGN KEY (id_persona) REFERENCES public.persona(id);


--
-- Name: detalle_reserva_instalacion fkc6vcioom3codfbemrgi7va9i7; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.detalle_reserva_instalacion
    ADD CONSTRAINT fkc6vcioom3codfbemrgi7va9i7 FOREIGN KEY (id_instalacion) REFERENCES public.instalacion(id);


--
-- Name: mantenimiento_equipo fkcn1p4imngxhp4tsg7272dxklm; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_equipo
    ADD CONSTRAINT fkcn1p4imngxhp4tsg7272dxklm FOREIGN KEY (id_reserva) REFERENCES public.reserva(id);


--
-- Name: municipio fke1way3pa23l5j480h48x5tp65; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.municipio
    ADD CONSTRAINT fke1way3pa23l5j480h48x5tp65 FOREIGN KEY (id_departamento) REFERENCES public.departamento(id);


--
-- Name: equipo fkedmef5567kb2dddb2ymvlfsup; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.equipo
    ADD CONSTRAINT fkedmef5567kb2dddb2ymvlfsup FOREIGN KEY (id_instalacion) REFERENCES public.instalacion(id);


--
-- Name: detalle_reserva_instalacion fken7cytqec64f6asmemsdejgce; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.detalle_reserva_instalacion
    ADD CONSTRAINT fken7cytqec64f6asmemsdejgce FOREIGN KEY (id_reserva) REFERENCES public.reserva(id);


--
-- Name: pais fkeskjwgmjcc5h5n1eh62de0phm; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.pais
    ADD CONSTRAINT fkeskjwgmjcc5h5n1eh62de0phm FOREIGN KEY (id_continente) REFERENCES public.continente(id);


--
-- Name: persona fkfmpcq2g2sm7s5wb3hrbp0pky; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.persona
    ADD CONSTRAINT fkfmpcq2g2sm7s5wb3hrbp0pky FOREIGN KEY (id_rol) REFERENCES public.rol(id);


--
-- Name: detalle_reserva_equipo fkfy8j4oippo6bcrttpy7ltkm8o; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.detalle_reserva_equipo
    ADD CONSTRAINT fkfy8j4oippo6bcrttpy7ltkm8o FOREIGN KEY (id_instalacion_destino) REFERENCES public.instalacion(id);


--
-- Name: mantenimiento_instalacion fkgya2c7auipq6xc592ey5h2xu7; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_instalacion
    ADD CONSTRAINT fkgya2c7auipq6xc592ey5h2xu7 FOREIGN KEY (id_instalacion) REFERENCES public.instalacion(id);


--
-- Name: permiso_rol_entidad fkhmvq44b43phmu7wt15kade7jb; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.permiso_rol_entidad
    ADD CONSTRAINT fkhmvq44b43phmu7wt15kade7jb FOREIGN KEY (id_permiso) REFERENCES public.permiso(id);


--
-- Name: permiso_rol_entidad fki5cu73ysv335gmh9cwdx7y5bq; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.permiso_rol_entidad
    ADD CONSTRAINT fki5cu73ysv335gmh9cwdx7y5bq FOREIGN KEY (id_rol) REFERENCES public.rol(id);


--
-- Name: mantenimiento_equipo fkj4o67afqmilj9cmin3d0hc2uc; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_equipo
    ADD CONSTRAINT fkj4o67afqmilj9cmin3d0hc2uc FOREIGN KEY (id_equipo) REFERENCES public.equipo(id);


--
-- Name: campus fkldegt7wgeloavt4n7702lhtx8; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.campus
    ADD CONSTRAINT fkldegt7wgeloavt4n7702lhtx8 FOREIGN KEY (id_municipio) REFERENCES public.municipio(id);


--
-- Name: instalacion fkod8t77q6ynvuw931gbe1j6yio; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.instalacion
    ADD CONSTRAINT fkod8t77q6ynvuw931gbe1j6yio FOREIGN KEY (id_campus) REFERENCES public.campus(id);


--
-- Name: mantenimiento_instalacion fkojjlspfdmgdn1rasysxp6letb; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_instalacion
    ADD CONSTRAINT fkojjlspfdmgdn1rasysxp6letb FOREIGN KEY (id_reserva) REFERENCES public.reserva(id);


--
-- Name: mantenimiento_equipo fkppqm9fr6rwhii2cqyhuxcac1p; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.mantenimiento_equipo
    ADD CONSTRAINT fkppqm9fr6rwhii2cqyhuxcac1p FOREIGN KEY (id_categoria_mantenimiento_equipo) REFERENCES public.categoria_mantenimiento_equipo(id);


--
-- Name: detalle_reserva_equipo fksyl6ik6pbnoc5wmsu9fmbkyyw; Type: FK CONSTRAINT; Schema: public; Owner: sebastianzambrano
--

ALTER TABLE ONLY public.detalle_reserva_equipo
    ADD CONSTRAINT fksyl6ik6pbnoc5wmsu9fmbkyyw FOREIGN KEY (id_equipo) REFERENCES public.equipo(id);


--
-- PostgreSQL database dump complete
--

