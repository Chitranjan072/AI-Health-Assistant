document.getElementById("diagnosisForm")?.addEventListener("submit", async (e) => {
  e.preventDefault();

  const symptoms = document.getElementById("symptoms").value;
  const age = document.getElementById("age").value;

  const formData = `symptoms=${encodeURIComponent(symptoms)}&age=${encodeURIComponent(age)}`;

  const res = await fetch("http://localhost:8080/diagnose", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: formData
  });

  const result = await res.text();
  localStorage.setItem("diagnosisResult", result);
  window.location.href = "analyzing.html";
});

function toggleDirection() {
  const ambulance = document.querySelector(".ambulance");
  const current = ambulance.style.animationDirection || "normal";
  ambulance.style.animationDirection = current === "reverse" ? "normal" : "reverse";
}
