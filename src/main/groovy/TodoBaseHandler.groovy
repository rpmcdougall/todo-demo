import groovy.transform.CompileStatic
import ratpack.exec.Promise
import ratpack.func.Action
import ratpack.func.Function
import ratpack.groovy.handling.GroovyByMethodSpec
import ratpack.groovy.handling.GroovyContext
import ratpack.handling.ByMethodSpec
import ratpack.handling.Context
import ratpack.handling.InjectionHandler
import ratpack.http.Response
import ratpack.jackson.Jackson

@CompileStatic
class TodoBaseHandler extends InjectionHandler {
    void handle(Context ctx, TodoRepository repository, String baseUrl) throws Exception {
        GroovyContext gtx = GroovyContext.from(ctx)
        Response response = ctx.response
        gtx.byMethod({ GroovyByMethodSpec method -> method
            .options {
                response.headers.set('Access-Control-Allow-Methods', 'OPTIONS, GET, POST, DELETE')
                response.send()
        }
        .get {
            repository.all
                    .map { todos -> todos.collect { TodoModel todo -> todo.baseUrl(baseUrl) } as List<TodoModel> }
                    .map(Jackson.&json)
                    .then(ctx.&render)
        }
        .post {
            Promise<TodoModel> todo = ctx.parse(Jackson.fromJson(TodoModel))
            todo
                    .flatMap(repository.&add as Function<TodoModel, Promise<TodoModel>>)
                    .map { TodoModel t -> t.baseUrl(baseUrl) }
                    .map(Jackson.&json)
                    .then(ctx.&render)
        }
        .delete { repository.deleteAll().then(response.&send) }
        } as Action<ByMethodSpec>)
    }
}