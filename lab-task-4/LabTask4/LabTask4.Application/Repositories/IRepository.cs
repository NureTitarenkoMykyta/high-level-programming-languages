using MongoDB.Bson;
using MongoDB.Driver;

namespace LabTask4.Application.Repositories;

public interface IRepository<T> where T : class
{
    Task<IEnumerable<T>> GetAllAsync();
    Task AddAsync(T entity, IClientSessionHandle? session = null);
    Task<bool> UpdateAsync(T entity, IClientSessionHandle? session = null);
    Task<bool> DeleteAsync(ObjectId id, IClientSessionHandle? session = null);
    Task<T?> GetByIdAsync(ObjectId id);
}
