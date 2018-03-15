import org.jooq.DSLContext
import org.jooq.SQLDialect

import javax.sql.DataSource

import static ratpack.groovy.Groovy.ratpack
import ratpack.hikari.HikariModule

ratpack {
    bindings {
        module(HikariModule) { config ->
            config.dataSourceClassName = 'org.h2.jdbcx.JdbcDataSource'
            config.addDataSourceProperty('URL', "jdbc:h2:mem:tood;INIT=RUNSCRIPT FROM 'classpath:/init.sql'")
        }
    }
    handlers {
        all(new CORSHandler())
        get('blocking'){
            DataSource ds = get(DataSource)
            DSLContext dsl = DSL.using(ds, SQLDialect.H2)
            def todos = dsl.select().from(Todo.TODO).fetchMaps()
            render(Jackson.json(todos))
        }
    }
} // TODO: 9.2 https://danhyun.github.io/2016-gr8confeu-rapid-ratpack-groovy/#defining_the_schema