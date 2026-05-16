using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.AspNetCore.OutputCaching;

namespace LabTask4.Attributes;

[AttributeUsage(AttributeTargets.Method)]
public class EvictCacheAttribute(params string[] tags) : Attribute, IAsyncActionFilter
{
    public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
    {
        var executedContext = await next();

        if (executedContext.Exception == null &&
            executedContext.Result is StatusCodeResult { StatusCode: >= 200 and < 300 } or ObjectResult { StatusCode: null or (>= 200 and < 300) })
        {
            var cacheStore = context.HttpContext.RequestServices.GetRequiredService<IOutputCacheStore>();

            foreach (var tag in tags)
            {
                await cacheStore.EvictByTagAsync(tag, context.HttpContext.RequestAborted);
            }
        }
    }
}