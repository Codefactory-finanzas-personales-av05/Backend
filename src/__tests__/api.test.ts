import { api } from '@/lib/api';

const mockJson = jest.fn();
const mockFetch = jest.fn();

beforeEach(() => {
  mockJson.mockResolvedValue({ status: 200, mensaje: 'ok', data: null });
  mockFetch.mockResolvedValue({ json: mockJson });
  global.fetch = mockFetch;
});

afterEach(() => {
  jest.clearAllMocks();
});

describe('api.registro', () => {
  it('debería hacer una petición POST al endpoint de registro', async () => {
    await api.registro({ correo: 'a@b.com', contrasena: '123', confirmarContrasena: '123' });
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/registro'),
      expect.objectContaining({ method: 'POST' })
    );
  });

  it('debería enviar Content-Type: application/json', async () => {
    await api.registro({ correo: 'a@b.com', contrasena: '123', confirmarContrasena: '123' });
    expect(mockFetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
      })
    );
  });
});

describe('api.login', () => {
  it('debería llamar al endpoint /login', async () => {
    await api.login({ correo: 'a@b.com', contrasena: '123' });
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/login'),
      expect.any(Object)
    );
  });
});

describe('api.categorias', () => {
  it('debería incluir el header Authorization con el token', async () => {
    await api.categorias('mi-token-secreto');
    const [, opciones] = mockFetch.mock.calls[0];
    expect(opciones.headers).toHaveProperty('Authorization', 'Bearer mi-token-secreto');
  });
});

describe('api.historial', () => {
  it('debería pedir la página 0 por defecto', async () => {
    await api.historial('token');
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('pagina=0'),
      expect.any(Object)
    );
  });

  it('debería pedir tamaño de página 10 por defecto', async () => {
    await api.historial('token');
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('tamano=10'),
      expect.any(Object)
    );
  });
});

describe('api.verificar', () => {
  it('debería llamar al endpoint /verificar con método POST', async () => {
    await api.verificar({ correo: 'a@b.com', codigo: '123456' });
    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/verificar'),
      expect.objectContaining({ method: 'POST' })
    );
  });
});

describe('api.registrarTransaccion', () => {
  it('debería enviar el body de la transacción con método POST', async () => {
    const body = { nombre: 'Café', monto: 5000, movimientoEn: '2026-05-12T10:00:00', tipo: 'GASTO' as const, idCategoria: 4 };
    await api.registrarTransaccion('token', body);
    expect(mockFetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({ method: 'POST', body: JSON.stringify(body) })
    );
  });
});
