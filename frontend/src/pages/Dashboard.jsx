import Layout from "../components/Layout";

function Dashboard() {
  return (
    <Layout>
      <div className="h-48 bg-[#2F6FED] rounded-xl shadow-md p-6 flex flex-col justify-center">
        <h1 className="text-3xl font-heading font-bold text-white">¡Bienvenida, Manuela!</h1>

        <p className="font-body text-white/90 mt-2">
          Continúa aprendiendo y revisa tu progreso.
        </p>
      </div>
    </Layout>
  );
}

export default Dashboard;
