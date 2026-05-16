using LabTask4.Application.Repositories;
using MongoDB.Bson;
using MongoDB.Driver;

namespace LanguageDuel.Infrastructure.Repositories;

public class Repository<T>(IMongoDatabase database) : IRepository<T> where T : class
{
    protected IMongoDatabase Database { get; set; } = database;

    protected IMongoCollection<T> Collection { get; set; } = database.GetCollection<T>($"{typeof(T).Name}s");

    public async Task<T?> GetByIdAsync(ObjectId id)
    {
        var filter = Builders<T>.Filter.Eq("Id", id);
        return await Collection.Find(filter).FirstOrDefaultAsync();
    }

    public async Task<IEnumerable<T>> GetAllAsync()
    {
        return await Collection.Find(_ => true).ToListAsync();
    }

    public async Task AddAsync(T entity, IClientSessionHandle? session = null)
    {
        if (session != null)
            await Collection.InsertOneAsync(session, entity);
        else
            await Collection.InsertOneAsync(entity);
    }

    public async Task<bool> UpdateAsync(T entity, IClientSessionHandle? session = null)
    {
        var id = GetIdValue(entity);
        var filter = Builders<T>.Filter.Eq("_id", ObjectId.Parse(id));
    
        var result = session != null 
            ? await Collection.ReplaceOneAsync(session, filter, entity)
            : await Collection.ReplaceOneAsync(filter, entity);

        return result.MatchedCount > 0;
    }

    public async Task<bool> DeleteAsync(ObjectId id, IClientSessionHandle? session = null)
    {
        var filter = Builders<T>.Filter.Eq("Id", id);
        var result = session != null 
            ? await Collection.DeleteOneAsync(session, filter)
            : await Collection.DeleteOneAsync(filter);

        return result.DeletedCount > 0;
    }

    private static string GetIdValue(T entity)
    {
        var value = entity.GetType().GetProperty("Id")?.GetValue(entity, null);
               
        return value?.ToString() ?? string.Empty;
    }
}